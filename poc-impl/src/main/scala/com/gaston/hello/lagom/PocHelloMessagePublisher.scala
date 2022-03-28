package com.gaston.hello.lagom

import akka.Done
import slick.dbio.DBIOAction
import slick.jdbc.PostgresProfile.api._
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide

import scala.concurrent.Future

class PocHelloMessagePublisher(
  readSide: SlickReadSide,
  kafkaHelper: KafkaHelper
) extends ReadSideProcessor[HelloEvent] {
  override def buildHandler(): ReadSideProcessor.ReadSideHandler[HelloEvent] =
    readSide
      .builder[HelloEvent]("hello-read-side")
      .setEventHandler[HelloPersisted](handleHelloPersisted)
      .build

  private def handleHelloPersisted(
    helloPersisted: EventStreamElement[HelloPersisted]
  ): DBIOAction[Future[Done], NoStream, Effect] = {
    DBIOAction.successful(
      kafkaHelper.sendHelloMessage(PocHelloMessage(helloPersisted.event.msg))
    )
  }

  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] =
    Set(HelloEvent.Tag)
}
