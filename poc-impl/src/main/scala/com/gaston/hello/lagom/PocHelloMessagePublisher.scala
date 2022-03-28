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
      .setGlobalPrepare(
        DBIO.seq(
          sqlu"DROP TABLE IF EXISTS public.journal",
          sqlu"""
              CREATE TABLE IF NOT EXISTS public.journal (
                ordering BIGSERIAL,
                persistence_id VARCHAR(255) NOT NULL,
                sequence_number BIGINT NOT NULL,
                deleted BOOLEAN DEFAULT FALSE,
                tags VARCHAR(255) DEFAULT NULL,
                message BYTEA NOT NULL,
                PRIMARY KEY(persistence_id, sequence_number)
              )
            """,
          sqlu"DROP TABLE IF EXISTS public.snapshot",
          sqlu"""
              CREATE TABLE IF NOT EXISTS public.snapshot (
                persistence_id VARCHAR(255) NOT NULL,
                sequence_number BIGINT NOT NULL,
                created BIGINT NOT NULL,
                snapshot BYTEA NOT NULL,
                PRIMARY KEY(persistence_id, sequence_number)
              )
            """,
          sqlu"DROP TABLE IF EXISTS public.read_side_offsets",
          sqlu"""
              CREATE TABLE public.read_side_offsets (
                read_side_id VARCHAR(255), tag VARCHAR(255),
                sequence_offset bigint, time_uuid_offset char(36),
                PRIMARY KEY (read_side_id, tag)
              )
              """
        )
      )
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
