package com.scalamc.packets

import java.io.{ByteArrayInputStream, DataOutput, DataOutputStream, File}
import java.util.UUID

import akka.util.ByteString
import com.scalamc.models.{Position, ProtocolVersion}
import com.scalamc.models.utils.VarLong
import com.scalamc.models.enums.PacketEnum
import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.models.metadata.{EntityMetadata, EntityMetadataRaw}
import com.scalamc.models.utils.{VarInt, VarLong}
import com.scalamc.utils.{ByteBuffer, ByteStack, ClassFieldsCache}
import com.scalamc.utils.BytesUtils._
import com.xorinc.scalanbt.tags._
import com.xorinc.scalanbt.io._
import enumeratum.{CirceEnum, EnumEntry}
import org.clapper.classutil.{ClassFinder, ClassInfo}

import scala.util.Try
import scala.reflect.runtime.{universe, currentMirror => rm}
import org.reflections.Reflections

import scala.annotation.ClassfileAnnotation
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.api.JavaUniverse


sealed trait PacketDirection extends EnumEntry

case object PacketDirection extends enumeratum.Enum[PacketDirection] with CirceEnum[PacketDirection] {
  case object Server  extends PacketDirection
  case object Client extends PacketDirection

  val values = findValues
}


sealed trait PacketState extends EnumEntry

case object PacketState extends enumeratum.Enum[PacketState] with CirceEnum[PacketState] {
  case object Login  extends PacketState
  case object Playing extends PacketState
  case object Status  extends PacketState

  val values = findValues
}

//object PacketState extends Enumeration {
//  val Login, Playing, Status = Value
//}
//object PacketDirection extends Enumeration {
//  val Server, Client = Value
//}

case class PacketInfo(var id: Int,
                      state: PacketState = PacketState.Playing,
                      direction: PacketDirection){
  override def equals(obj: scala.Any): Boolean ={
    obj match {
      case inf: PacketInfo =>
        inf.id == id && inf.direction == direction && inf.state == state
      case _ =>
        false
    }
  }
}

object Packet{

  var protocols: Map[Int, Map[PacketInfo, Packet]] = Map(-1 -> packets)
  var protocolVersions: Map[Int, ProtocolVersion] = Map()

  def updateProtocols(newProtocols: Map[Int, Map[PacketInfo, _ <: Packet]] ): Unit ={
    protocols = newProtocols + (-1 -> packets)
    println("update protocols"+protocols.filter(p => p._1 != -1))
    println(protocols(-1).find(_._1.id == 0x04).get._2.getClass)
    protocols foreach {p => println(p._1)}
  }

  def newInstance(className: String): Option[Packet] = Try {
    val m = universe.runtimeMirror(getClass.getClassLoader)
    if (className.endsWith("$")) {
      m.reflectModule(m.staticModule(className.init)).instance.asInstanceOf[Packet]
    } else {
      Class.forName(className).newInstance().asInstanceOf[Packet]
    }
  }.toOption

  lazy val packets: Map[PacketInfo, _ <: Packet] = {
    val reflections = new Reflections("com.scalamc.packets")
    val subclasses = reflections.getSubTypesOf(classOf[Packet])

    subclasses.asScala map {p => val pac = p.newInstance(); pac.packetInfo -> pac} toMap
  }

  def getNewPacketId(packetInfo: PacketInfo, protocolVersion: Int): Int = {
    if(protocols.contains(protocolVersion)) protocols(protocolVersion).find(pc => pc._1 == packetInfo).map(p => p._2.packetInfo).getOrElse(packetInfo).id
    else packetInfo.id
  }

  def getPacket(id: Byte, protocolId: Int, state: PacketState = PacketState.Playing, direction: PacketDirection = PacketDirection.Server): Packet ={
    //if(protocols.contains(protocolId))
    //packets.find((p) => (p._1.ids(protocolId) == id || p._1.ids(-1) == id) && p._1.state == state && p._1.direction == direction).get._2
    val info = PacketInfo(id, state, direction)
    if(protocols.contains(protocolId)) {
      val pack = protocols(protocolId).find(pc => pc._2.packetInfo == info).getOrElse(protocols(-1).find(pc => pc._1 == info).orNull)
      if(pack._1 == null) protocols(-1).find(pc => pc._1 == info).orNull._2
      else if(pack._2 == null) protocols(-1).find(pc => pc._1 == PacketInfo(pack._1.id, state, direction)).orNull._2
      else pack._2
    } else{
      try {
        protocols(-1).find(pc => pc._1 == info).orNull._2
      } catch {
        case _: Exception => throw new IllegalStateException(s"Cannot find packet (id: $id direction: $direction protocolId: $protocolId state: $state)")
      }
    }
      //val pack = protocols.getOrElse(protocolId, protocols(-1)).find(pc => pc._1 == PacketInfo(id, state, direction)).orElse(protocols(-1).find(pc => pc._1 == PacketInfo(id, state, direction))).orNull._2

  }


  def fromByteBuffer(buf: ByteBuffer, state: PacketState = PacketState.Playing)(implicit protocolId: Int): Packet ={
    val packet = getPacket(buf(0),protocolId, state)
    buf.remove(0)
    packet.read(buf)
    packet
  }

  implicit def pack2ArrayBuff(pack: Packet)(implicit protocolId: Int): ByteBuffer = pack.write()

  implicit def packet2ByteStrign(pack: Packet)(implicit protocolId: Int) = ByteString(pack.toArray)
}



abstract class Packet(val packetInfo: PacketInfo) {
  import scala.reflect.runtime.universe._
  override def clone: Packet = this.getClass.newInstance()
  implicit class TermSymbolExtend(ts: TermSymbol)(implicit instanceMirror: InstanceMirror){
    implicit def :=(any: Any): Unit = instanceMirror.reflectField(ts).set(any)
  }

  val instanceMirror: universe.InstanceMirror = rm.reflect(this)

  //Set default id
  //packetInfo.ids = packetInfo.ids.withDefault(_ => 0xFF.toByte)

  //val fields: List[(Any, universe.TermSymbol)] =

  def read(byteBuffer: ByteBuffer): Unit ={
    val stack = new ByteStack(byteBuffer.toArray)
    readFields(stack, this)
    //println("read end ",javax.xml.bind.DatatypeConverter.printHexBinary(stack.toArray))
  }

  def readFields(stack: ByteStack, obj: Any): Unit ={
    val classFields: List[(Any, universe.TermSymbol)] = ClassFieldsCache.getFields(obj)
    implicit val instMirror: universe.InstanceMirror = rm.reflect(obj)

    for((name, field) <- classFields){
      val res = readField(stack, name)
      if(res != Unit) field := res
    }
  }

  def readField(stack: ByteStack, name: Any): Any ={
    name match {
      case _: String =>
        val len = stack.popVarInt()
        ByteString(stack.popWith(len).toArray).decodeString("utf-8")
      case _: VarInt =>
        VarInt(stack.popVarInt())
      case _: Double =>
        stack.popDouble()
      case _: Float =>
        stack.popFloat()
      case _: Boolean =>
        if (stack.pop()==0) false else true
      case _: Long =>
        stack.popLong()
      case _: Byte =>
        stack.pop()
      case _: Array[Byte] =>
        stack.popWith(stack.popVarInt()).toArray
      case _: Short =>
        stack.popShort()
      case _: Int =>
        stack.popInt()
      case _: com.scalamc.models.Position =>
        val value = stack.popLong()
        val x = value >> 38
        // signed
        val y = value >> 26 & 0xfff
        // unsigned
        // this shifting madness is used to preserve sign
        val z = value << 38 >> 38
        com.scalamc.models.Position(x.toInt, y.toInt, z.toInt)


        //println(e.enum.values.find(v => v.id == stack.pop().toInt))
        //println(e.enum.values.mkString)
        //field := e.getClass.getConstructors()(0).newInstance(stack.pop())
      //  println("read enum")
      //  readField(stack, e.value, rm.reflect(e.value).symbol.asTerm)
      case _: TagCompound =>
        readNBT(new ByteArrayInputStream(stack.toArray))._2
      case op: Option[Any] =>
        if(stack.nonEmpty){
          return Some(readField(stack, op.get))
        }
        None
      case other =>
        readFields(stack, other)
        Unit
    }
  }

  def write()(implicit protocolId: Int): ByteBuffer ={
    var buff: ByteBuffer = new ByteBuffer()

    //var id = packetInfo.ids(protocolId)
    //buff += (if(id==0xFF.toByte) packetInfo.ids(-1) else id)
    buff += Packet.getNewPacketId(packetInfo, protocolId).toByte//packetInfo.id.toByte
    //println("id",Packet.getNewPacketId(packetInfo, protocolId),this)

    buff += writeFields(ClassFieldsCache.getFields(this), instanceMirror, isMetadata = false).toArray

    var lenBuff = new ByteBuffer()
    lenBuff.writeVarInt(buff.length)

    lenBuff+buff.toArray
  }

  def writeFieldValue(buff: ByteBuffer, ind: Int, value: Any, isMetadata: Boolean): Unit ={
    value match {
      case str: String =>
        if(isMetadata) buff.writeVarInt(3)
        buff += str
      case option: Option[Any] => if(option.isEmpty) buff += 0x00.toByte else writeFieldValue(buff, ind, option.get, isMetadata)
      case int: Int =>
        buff += int
      case varInt: VarInt =>
        if(isMetadata) buff.writeVarInt(1)
        buff.writeVarInt(varInt.int)
      case enum: EnumVal =>
        writeFieldValue(buff, ind, enum.value, isMetadata = false)
      case bool: Boolean =>
        if(isMetadata) buff.writeVarInt(6)
        buff += (if(bool) 1.toByte else 0.toByte)
      case byte: Byte =>
        if(isMetadata) buff.writeVarInt(0)
        buff += byte
      case pos: com.scalamc.models.Position => buff += pos.toLong
      case long: VarLong =>
      case long: Long => buff += long
      case double: Double => buff += double
      case float: Float =>
        if(isMetadata) buff.writeVarInt(2)
        buff += float
      case short: Short =>
        buff += short
      case buf: ByteBuffer =>
        buff.writeVarInt(buf.size)
        buff += buf.toArray
      case arr: Array[Byte] =>
        buff += arr
      case uuid: UUID =>
        buff += uuid.getMostSignificantBits
        buff += uuid.getLeastSignificantBits
      case arBuf: ArrayBuffer[Any] =>
        buff.writeVarInt(arBuf.size)
        if(arBuf.nonEmpty) {
          if(rm.reflect(arBuf(0)).symbol.annotations.exists(_.tree.tpe.typeSymbol == typeOf[NotParsable].typeSymbol))
            arBuf.foreach(el => writeFieldValue(buff, ind, el, isMetadata = false))
          else
            arBuf.foreach(el => buff += writeFields(ClassFieldsCache.getFields(el), rm.reflect(el), isMetadata = false).toArray)
        }
      case tag: TagCompound =>
        import java.io.ByteArrayOutputStream
        import java.io.DataOutput
        val boas = new ByteArrayOutputStream()
        val out = new DataOutputStream(boas)
        writeNBT(out)("", tag)
        buff += boas.toByteArray
      case other =>
        buff += writeFields(ClassFieldsCache.getFields(other), rm.reflect(other), other.getClass == classOf[EntityMetadataRaw]).toArray
    }
  }

  def writeField(buff: ByteBuffer, ind: Int, t: Any, acc: scala.reflect.runtime.universe.TermSymbol, im: InstanceMirror, isMetadata: Boolean): Unit ={
    if(isMetadata) buff += ind.toByte
    writeFieldValue(buff, ind, im.reflectField(acc).get, isMetadata)

  }

  def writeFields(fields: List[(Any, scala.reflect.runtime.universe.TermSymbol)], im: InstanceMirror, isMetadata: Boolean): ByteBuffer={
    var buff: ByteBuffer = new ByteBuffer()

    //if(isMetadata) buff.writeVarInt(fields.size)
    for(((t, acc), ind) <- fields.zipWithIndex){
      writeField(buff, ind, t, acc, im, isMetadata)
      //println(javax.xml.bind.DatatypeConverter.printHexBinary(buff.toArray))
    }

    if(isMetadata) buff += 0xFF.toByte

    buff
  }

}
