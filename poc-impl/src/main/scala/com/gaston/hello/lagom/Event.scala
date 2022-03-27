package com.gaston.hello.lagom

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.Json

sealed trait HelloEvent extends AggregateEvent[HelloEvent] {
  override def aggregateTag: AggregateEventTag[HelloEvent] = HelloEvent.Tag
}

object HelloEvent {
  val Tag: AggregateEventTag[HelloEvent] =
    AggregateEventTag[HelloEvent]
}

case class HelloPersisted(msg: String) extends HelloEvent

object HelloPersisted {
  implicit val format = Json.format[HelloPersisted]
}
