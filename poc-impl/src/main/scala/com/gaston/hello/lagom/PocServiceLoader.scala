package com.gaston.hello.lagom

import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{
  JsonSerializer,
  JsonSerializerRegistry
}
import com.lightbend.lagom.scaladsl.server.{
  LagomApplication,
  LagomApplicationContext,
  LagomApplicationLoader
}
import com.softwaremill.macwire.wire
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents

class PocServiceLoader extends LagomApplicationLoader {
  override def load(context: LagomApplicationContext): LagomApplication =
    new PocApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext) =
    new PocApplication(context) with LagomDevModeComponents
}

abstract class PocApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents
    with HikariCPComponents
    with SlickPersistenceComponents {
  override lazy val lagomServer = serverFor[PocServiceAPI](wire[PocServiceImpl])

  implicit lazy val system = actorSystem

  lazy val kafkaHelper = wire[KafkaHelper]

  lazy val consumer = Consumer.committableSource(
    ConsumerSettings(
      actorSystem.settings.config.getConfig("akka.kafka.consumer"),
      new StringDeserializer,
      new StringDeserializer
    ).withBootstrapServers(config.getString("kafka.brokers"))
      .withGroupId("poc.hello.group")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
    Subscriptions.topics("event.poc.hello")
  )
  readSide.register(wire[PocHelloMessagePublisher])

  override def jsonSerializerRegistry: JsonSerializerRegistry =
    new JsonSerializerRegistry {
      override def serializers: Seq[JsonSerializer[_]] = Seq(
        JsonSerializer[HelloPersisted]
      )
    }

}
