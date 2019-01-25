package com.scalamc.utils

import java.io.File
import java.net.URI

import io.circe.parser.decode

object ModulesLoader {
  def getClassLoader(dir: String): java.net.URLClassLoader ={
    def getListOfFiles(dir: String): List[URI] = {
      val file = new File(dir)
      if(file.exists())
        file.listFiles.filter(_.isFile)
          .filter(_.getName.endsWith(".jar"))
          .map(_.toURI).toList
      else List()
    }
    new java.net.URLClassLoader(getListOfFiles(dir).map(_.toURL).toArray, this.getClass.getClassLoader)

  }
}
