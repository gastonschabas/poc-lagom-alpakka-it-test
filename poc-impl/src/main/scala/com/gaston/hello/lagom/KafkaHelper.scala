package com.gaston.hello.lagom

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.api.Configuration
import play.api.libs.json.Json

import java.util.UUID
import scala.concurrent.Future

class KafkaHelper(configuration: Configuration)(implicit
  actorSystem: ActorSystem
) {

  def sendHelloMessage(msg: PocHelloMessage): Future[Done] = Source
    .single(
      new ProducerRecord(
        "event.poc.hello",
        UUID.randomUUID().toString,
        Json.toJson(msg).toString
      )
    )
    .runWith(
      Producer.plainSink(
        ProducerSettings(
          actorSystem.settings.config.getConfig("akka.kafka.producer"),
          new StringSerializer,
          new StringSerializer
        ).withBootstrapServers(configuration.get[String]("kafka.brokers"))
      )
    )

}
