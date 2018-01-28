package com.scalamc.packets

import java.io.File
import java.util.UUID

import akka.util.ByteString
import com.scalamc.models.{VarInt, VarLong}
import com.scalamc.models.enums.PacketEnum
import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.utils.{ByteBuffer, ByteStack}
import com.scalamc.utils.BytesUtils._
import org.clapper.classutil.{ClassFinder, ClassInfo}

import scala.util.Try
import scala.reflect.runtime.universe
import scala.reflect.runtime.{currentMirror => rm}
import org.reflections.Reflections

import scala.annotation.ClassfileAnnotation
import scala.collection.JavaConverters._
import scala.reflect.api.JavaUniverse

//object PacketState extends Enumeration{
//  type PacketState = Value
//  val Login, Playing = Value
//}

object Packet{

  def newInstance(className: String): Option[Packet] = Try {
    val m = universe.runtimeMirror(getClass.getClassLoader)
    if (className.endsWith("$")) {
      m.reflectModule(m.staticModule(className.init)).instance.asInstanceOf[Packet]
    } else {
      Class.forName(className).newInstance().asInstanceOf[Packet]
    }
  }.toOption

  lazy val packets = {
//    val finder = ClassFinder(Seq(new File(getClass.getResource(".").toURI)))
//    val classes = finder.getClasses  // classes is an Iterator[ClassInfo]
//    //val classMap = ClassFinder.classInfoMap(classes) // runs iterator out, once
//    val packets = classes.collect{
//      case ci: ClassInfo if ci.superClassName == getClass.getName.replace("$", "") => ci
//    }
//    packets

    val reflections = new Reflections("com.scalamc.packets")
    val subclasses = reflections.getSubTypesOf(classOf[Packet])

    subclasses.asScala map (p=> (p.newInstance().packetInfo, p))

    //universe.typeOf[PacketInfo].members

  }

  def getPacket(id: Byte, protocolId: Int, state: PacketState.Value = PacketState.Playing, direction: PacketDirection.Value = PacketDirection.Server) =
    packets.find((p)=>(p._1.ids(protocolId)==id||p._1.ids(-1)==id)&&p._1.state==state&&p._1.direction==direction).get._2

  def fromByteBuffer(buf: ByteBuffer, state: PacketState.Value = PacketState.Playing)(implicit protocolId: Int): Packet ={
    val packet = getPacket(buf(0),protocolId, state).newInstance()
    buf.remove(0)
    packet.read(buf)
    packet
  }

  implicit def pack2ArrayBuff(pack: Packet)(implicit protocolId: Int): ByteBuffer = pack.write()

  implicit def packet2ByteStrign(pack: Packet)(implicit protocolId: Int) = ByteString(pack.toArray)
}

object PacketState extends Enumeration {
  val Login, Playing, Status = Value
}
object PacketDirection extends Enumeration {
  val Server, Client = Value
}

case class PacketInfo(var ids: Map[Int, Byte],
                      state: PacketState.Value = PacketState.Playing,
                      direction: PacketDirection.Value,
                      var fields: List[(Any, scala.reflect.runtime.universe.TermSymbol)] = null)

abstract class Packet(val packetInfo: PacketInfo) {
  import scala.reflect.runtime.universe._

  implicit class TermSymbolExtend(ts: TermSymbol)(implicit instanceMirror: InstanceMirror){
    implicit def :=(any: Any) = instanceMirror.reflectField(ts).set(any)
  }

  implicit val instanceMirror = rm.reflect(this)

  //Set default id
  packetInfo.ids = packetInfo.ids.withDefault(_ => 0xFF.toByte)

  packetInfo.fields = rm.classSymbol(this.getClass).toType.members.collect {
    case m: TermSymbol if m.isVar => m
  }.toList.reverse.map{s => instanceMirror.reflectField(s).get->s}
  //println(packetInfo.id, packetInfo.fields)

  def read(byteBuffer: ByteBuffer): Unit ={
    var stack = new ByteStack(byteBuffer.toArray)

//    val accessors = rm.classSymbol(this.getClass).toType.members.collect {
//      case m: TermSymbol if m.isVar => m
//    }.toList.reverse
    //println("start decode", javax.xml.bind.DatatypeConverter.printHexBinary(byteBuffer.toArray))
    for((name, field) <- packetInfo.fields){
      //println("stack", javax.xml.bind.DatatypeConverter.printHexBinary(stack.toArray))
      //println("sig", field.name)
      name match {
        case _: String =>
          val len = stack.popVarInt()
          //instanceMirror.reflectField(acc).set(ByteString(stack.popWith(len).toArray).decodeString("utf-8"))
          field := ByteString(stack.popWith(len).toArray).decodeString("utf-8")
        case _: VarInt =>
          //instanceMirror.reflectField(acc).set(VarInt(stack.popVarInt()))
          field := VarInt(stack.popVarInt())
        case _: Double =>
          //instanceMirror.reflectField(acc).set(stack.popDouble())
          field := stack.popDouble()
        case _: Float =>
          //instanceMirror.reflectField(acc).set(stack.popFloat())
          field := stack.popFloat()
        case _: Boolean =>
          //instanceMirror.reflectField(acc).set(if (stack.pop()==0) false else true)
          field := (if (stack.pop()==0) false else true)
        case _: Long =>
          //instanceMirror.reflectField(acc).set(stack.popLong())
          field := stack.popLong()
        case _: Byte =>
          //instanceMirror.reflectField(acc).set(stack.pop())
          field := stack.pop()
        case _: Array[Byte] =>
          //instanceMirror.reflectField(acc).set(stack.popWith(stack.popVarInt()).toArray)
          field := stack.popWith(stack.popVarInt()).toArray
        case _: Short =>
          field := stack.popShort()
        case _: Int =>
          field := stack.popInt()
      }

    }
  }

  def write()(implicit protocolId: Int): ByteBuffer ={
    var buff: ByteBuffer = new ByteBuffer()

    var id = packetInfo.ids(protocolId)
    buff += (if(id==0xFF.toByte) packetInfo.ids(-1) else id)
    //if(id==0xFF.toByte) println(packetInfo.ids)
//    val accessors = rm.classSymbol(this.getClass).toType.members.collect {
//      case m: MethodSymbol if m.isGetter && m.isPublic => m
//    }.toList.reverse


    for((t, acc) <- packetInfo.fields){
      //println(acc.name)
      instanceMirror.reflectField(acc).get match {
        case str: String => buff += str
        case int: Int => buff += int
        case varInt: VarInt => buff.writeVarInt(varInt.int)
        case enum: EnumVal => buff += enum.toBytes
        case bool: Boolean => buff += (if(bool) 1.toByte else 0.toByte)
        //case pi: PacketInfo => buff += pi.id
        case byte: Byte => buff += byte
        case pos: com.scalamc.models.Position => buff += pos.toLong
        case long: VarLong =>
        case long: Long => buff += long
        case double: Double => buff += double
        case float: Float => buff += float
        case buf: ByteBuffer =>
          buff.writeVarInt(buf.size)
          buff += buf.toArray
        case arr: Array[Byte] =>
          buff += arr
        case uuid: UUID =>
          buff += uuid.getMostSignificantBits
          buff += uuid.getLeastSignificantBits
      }
      //println(javax.xml.bind.DatatypeConverter.printHexBinary(buff.toArray))
    }
    var lenBuff = new ByteBuffer()
    lenBuff.writeVarInt(buff.length)

    lenBuff+buff.toArray
  }

}
