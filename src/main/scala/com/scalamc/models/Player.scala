package com.scalamc.models

import java.util.UUID

import com.scalamc.actors.Session

case class Player(var name: String, var uuid: UUID, var session: Session) {

}
