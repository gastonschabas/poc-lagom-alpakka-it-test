package com.gaston.hello.lagom

import akka.actor.ActorSystem
import akka.persistence.query.Offset
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import com.dimafeng.testcontainers.lifecycle.and
import com.dimafeng.testcontainers.scalatest.TestContainersForAll
import com.dimafeng.testcontainers.{KafkaContainer, PostgreSQLContainer}
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ReadSideTestDriver, ServiceTest}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.util.UUID

class PocHelloMessagePublisherTest
    extends AsyncFunSuite
    with Matchers
    with BeforeAndAfterAll
    with TestContainersForAll {

  override type Containers = KafkaContainer and PostgreSQLContainer

  override def startContainers(): and[KafkaContainer, PostgreSQLContainer] =
    KafkaContainer
      .Def()
      .start()
      .and(
        PostgreSQLContainer
          .Def(dockerImageName = "postgres:12.0-alpine")
          .start()
      )

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  lazy val server = withContainers { case kafka and postgresql =>
    ServiceTest.startServer(ServiceTest.defaultSetup.withJdbc())(context =>
      new PocApplication(context) with LocalServiceLocator {
        override def additionalConfiguration: AdditionalConfiguration =
          super.additionalConfiguration ++ Configuration(
            "db.default.url" -> postgresql.jdbcUrl,
            "db.default.username" -> postgresql.username,
            "db.default.password" -> postgresql.password,
            "db.default.driver" -> postgresql.driverClassName,
            "akka.kafka.consumer.kafka-clients.enable.auto.commit" -> true,
            "kafka.brokers" -> kafka.bootstrapServers,
            "akka.kafka.consumer.stop-timeout" -> 0
          ).underlying
        override lazy val readSide = new ReadSideTestDriver()
      }
    )
  }

  implicit lazy val actorSystem: ActorSystem = server.application.actorSystem

  test("testing read-side processor") {
    server.application.readSide
      .feed[HelloEvent](
        UUID.randomUUID().toString,
        HelloPersisted("persisted"),
        Offset.sequence(1)
      )
      .flatMap { _ =>
        val (consumer, probe) = server.application.consumer
          .map(x =>
            PocHelloMessage.format.reads(Json.parse(x.record.value())) match {
              case JsSuccess(value, _) => value
              case JsError(errors) =>
                fail(s"error parsing kafka message.\n${Json
                  .prettyPrint(JsError.toJson(errors))}")
            }
          )
          .toMat(TestSink.probe[PocHelloMessage])(Keep.both)
          .run
        for {
          assertion <- probe.requestNext() should be(
            PocHelloMessage("persisted")
          )
          _ <- consumer.stop()
          _ <- consumer.shutdown()
        } yield assertion
      }
  }

  test("testing read-side processor 2") {
    server.application.readSide
      .feed[HelloEvent](
        UUID.randomUUID().toString,
        HelloPersisted("persisted 2"),
        Offset.sequence(2)
      )
      .flatMap { _ =>
        val (consumer, probe) = server.application.consumer
          .map(x =>
            PocHelloMessage.format.reads(Json.parse(x.record.value())) match {
              case JsSuccess(value, _) => value
              case JsError(errors) =>
                fail(s"error parsing kafka message.\n${Json
                  .prettyPrint(JsError.toJson(errors))}")
            }
          )
          .toMat(TestSink.probe[PocHelloMessage])(Keep.both)
          .run
        for {
          assertion <- probe.requestNext() should be(
            PocHelloMessage("persisted 2")
          )
          _ <- consumer.stop()
          _ <- consumer.shutdown()
        } yield assertion
      }
  }
}
