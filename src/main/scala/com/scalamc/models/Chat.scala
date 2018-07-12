package com.scalamc.models

import io.circe.generic.JsonCodec

@JsonCodec case class Chat(var text: String = "",
                var bold: Option[Boolean] = None,
                var italic: Option[Boolean] = None,
                var underlined: Option[Boolean] = None,
                var strikethrough: Option[Boolean] = None,
                var obfuscated: Option[Boolean] = None,
                var color: Option[String] = None,
                var insertion: Option[String] = None) {
}
