package com.gaston.hello.lagom

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait PocServiceAPI extends Service {

  def ping: ServiceCall[NotUsed, String]

  def descriptor: Descriptor =
    Service
      .named("poc-service")
      .withCalls(Service.restCall(Method.GET, "/ping", ping _))

}
