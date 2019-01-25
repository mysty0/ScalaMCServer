package com.scalamc.actors

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props}
import com.scalamc.models.{PacketInfoJson, ProtocolInfo, ProtocolVersion}
import com.scalamc.packets.{Packet, PacketDirection, PacketInfo, PacketState}
import com.scalamc.utils.ModulesLoader
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.collection.mutable.ArrayBuffer

object ProtocolVersionManager{
  case class LoadProtocols(dir: String)
}

class ProtocolVersionManager extends Actor with ActorLogging{
  import ProtocolVersionManager._

  override def receive: Receive = {
    case LoadProtocols(dir) =>
      val classLoader = ModulesLoader.getClassLoader(dir)
      val infoFiles = classLoader.findResources("protocol.info")
      //println(ProtocolInfo(0, 0, "", Map(1 -> PacketInfo(2, PacketState.Playing, PacketDirection.Server),2 -> PacketInfo(2, PacketState.Playing, PacketDirection.Server)).map(m => m.swap), Map()).asJson)
      var protocols: ArrayBuffer[ProtocolVersion] = ArrayBuffer()
      while(infoFiles.hasMoreElements){
        val str = scala.io.Source.fromInputStream(infoFiles.nextElement().openStream()).mkString////
        val info = decode[ProtocolInfo](str).toOption
        if(info.isDefined){
          val inf = info.get
          inf.packetsIds foreach println
          val packets = {
            inf.packets.map { pc => pc._1 -> classLoader.loadClass(pc._2).asInstanceOf[Packet] }
          }

          protocols += ProtocolVersion(inf, if(inf.resolver.nonEmpty) Option(classLoader.loadClass(inf.resolver).asInstanceOf[Class[Actor]]) else None, packets)
        }
      }
      Packet.protocolVersions = protocols.map(pv => pv.info.protocolId -> pv).toMap
      protocols.filter{p=>if(!protocols.exists { pv => pv.info.protocolId == p.info.dependProtocol } && p.info.dependProtocol != -1){
                            log.warning(s"No depend protocol ${p.info.dependProtocol} fot protocol ${p.info.protocolId}"); true;} else false}

      def findPacket(info: PacketInfo, protocolVersion: ProtocolVersion, prevProtocols: Set[Int] = Set()): Option[Packet] = {
        if(protocolVersion == null) return None
        if(prevProtocols.contains(protocolVersion.info.protocolId)) return None
        var p = protocolVersion.packets.find(p => p._2.packetInfo.copy(p._1)==info).map(_._2)
        if(p.isEmpty)
          p = findPacket(protocolVersion.info.packetsIds.find(prevId => prevId._2 == info).getOrElse(-1, null)._2,
                        protocols(protocolVersion.info.dependProtocol),
                        prevProtocols+protocolVersion.info.protocolId)
        p
      }
      def merge[K, V](m1:Map[K, V], m2:Map[K, V]):Map[K, V] =
        (m1.keySet ++ m2.keySet) map { i: K => i -> m1.getOrElse(i, m2(i)) } toMap
      def chagePacketId(packet: Packet, newId: Int) : Packet ={
        var pack: Packet = packet.clone()
        pack.packetInfo.id = newId
        pack
      }
      Packet.updateProtocols(protocols.map(pv =>
        pv.info.protocolId -> merge(
                                    pv.info.packetsIds.map(p => p._2.copy(p._1) -> chagePacketId(findPacket(p._2,
                                      if(pv.info.dependProtocol != -1) protocols(pv.info.dependProtocol) else null).getOrElse(Packet.protocols(-1).find(pac => pac._1 == p._2.copy(p._1)).get._2), p._2.id)).map( p => p),
                                    pv.packets.map(pc => pc._2.packetInfo -> pc._2))).toMap)
      sender ! Success
  }
}
