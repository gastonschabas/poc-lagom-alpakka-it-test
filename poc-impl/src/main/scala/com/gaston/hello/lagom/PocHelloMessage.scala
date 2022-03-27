package com.gaston.hello.lagom

import play.api.libs.json.Json

case class PocHelloMessage(value: String)

object PocHelloMessage {
  implicit val format = Json.format[PocHelloMessage]
}
