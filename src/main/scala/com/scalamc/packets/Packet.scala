package com.scalamc.packets

import java.io.File

import akka.util.ByteString
import com.scalamc.models.VarInt
import com.scalamc.models.enums.PacketEnum
import com.scalamc.models.enums.PacketEnum.EnumVal
import com.scalamc.utils.ByteBuffer
import com.scalamc.utils.BytesUtils._
import org.clapper.classutil.{ClassFinder, ClassInfo}

import scala.util.Try
import scala.reflect.runtime.universe
import scala.reflect.runtime.{currentMirror => rm}
import org.reflections.Reflections

import scala.annotation.ClassfileAnnotation
import scala.collection.JavaConverters._

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

  def getPacket(id: Byte, state: PacketState.Value = PacketState.Playing, direction: PacketDirection.Value = PacketDirection.Server) = packets.find((p)=>p._1.id==id&&p._1.state==state&&p._1.direction==direction).get._2

  def fromByteBuffer(buf: ByteBuffer, state: PacketState.Value = PacketState.Playing): Packet ={
    val packet = getPacket(buf(0), state).newInstance()
    buf.take(0)
    packet.read(buf)
    packet
  }

  implicit def packet2ByteStrign(pack: Packet) = ByteString(pack.write().toArray)
}

object PacketState extends Enumeration {
  val Login, Playing = Value
}
object PacketDirection extends Enumeration {
  val Server, Client = Value
}

case class PacketInfo(id: Byte, state: PacketState.Value, direction: PacketDirection.Value)

abstract class Packet(val packetInfo: PacketInfo) {
  import scala.reflect.runtime.universe._
  def read(byteBuffer: ByteBuffer): Unit ={

    val accessors = rm.classSymbol(this.getClass).toType.members.collect {
      case m: MethodSymbol if m.isSetter && m.isPublic => m
    }

    val instanceMirror = rm.reflect(this)
    for(acc <- accessors){
      if(accessors.size == 1) {
        instanceMirror.reflectMethod(acc).apply(ByteString(byteBuffer.toArray).decodeString("utf-8"))
      }
    }
  }

  def write(): ByteBuffer ={
    var buff: ByteBuffer = new ByteBuffer()

    val accessors = rm.classSymbol(this.getClass).toType.members.collect {
      case m: MethodSymbol if m.isGetter && m.isPublic => m
    }.toList.reverse

    val instanceMirror = rm.reflect(this)
    for(acc <- accessors){
      println( instanceMirror.reflectMethod(acc).apply())
      instanceMirror.reflectMethod(acc).apply() match {
        case str: String => buff += str
        case int: Int => buff += int
        case varInt: VarInt => buff.writeVarInt(varInt.int)
        case enum: EnumVal => buff += enum.toBytes
        case bool: Boolean => buff += (if(bool) 1.toByte else 0.toByte)
        case pi: PacketInfo => buff += pi.id
        case byte: Byte => buff += byte
      }

    }
    var lenBuff = new ByteBuffer()
    lenBuff.writeVarInt(buff.length)

    lenBuff+buff.toArray
  }

}
