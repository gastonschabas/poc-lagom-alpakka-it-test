package com.gaston.hello.lagom
import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PocServiceImpl extends PocServiceAPI {
  override def ping: ServiceCall[NotUsed, String] =
    ServiceCall(_ => Future("pong"))
}
