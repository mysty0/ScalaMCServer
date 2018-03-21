package com.scalamc.utils

import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => rm}
import scala.collection.mutable


object ClassFieldsCache {
  var map = collection.mutable.Map[Any, List[(Any, scala.reflect.runtime.universe.TermSymbol)]]()



  def getFields(clas: Any): List[(Any, scala.reflect.runtime.universe.TermSymbol)]={
    val instanceMirror = rm.reflect(clas)
    map.getOrElse(clas,
      rm.classSymbol(clas.getClass).toType.members.collect {
        case m: TermSymbol if m.isVar => m
      }.toList.reverse.map{s => instanceMirror.reflectField(s).get->s})
  }

}
