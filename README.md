# POC Lagom Alpakka IT Test

An integration test to validate that a kafka message was published by read side processor using alpakka

# Table of Contents

- [Requirements](#requirements)
- [Description](#description)
- [How the project is built](#how-the-project-is-built)
    - [Stack](#stack)
    - [PocServiceLoader](#pocserviceloader)
- [KafkaHelper](#kafkahelper)
- [PocHelloMessagePublisher](#pochellomessagepublisher)
    - [Integration Test](#integration-test)

# Requirements

- [docker](https://www.docker.com/) 20.10.12
- [jdk](https://adoptopenjdk.net/) 11
- [scala](https://www.scala-lang.org/) 2.13.8
- [sbt](https://www.scala-sbt.org/) 1.6.2

# Description

A POC to start a [Kafka Container](https://hub.docker.com/r/confluentinc/cp-kafka/), a 
[PostrgeSQL Container](https://hub.docker.com/_/postgres) and a [Lagom Application](https://www.lagomframework.com/) to 
persist en event in the event journal, publish a kafka message in a topic and consume it in an Integration Test 

# How the project is built

## Stack

- [scala](https://www.scala-lang.org/) 2.13.8
- [Lagom](https://www.lagomframework.com/) 1.6.7
- [Alpakka](https://doc.akka.io/docs/alpakka/current/index.html) 3.0.0
- [ScalaTest](https://www.scalatest.org/) 3.3.3
- [Test Containers](https://github.com/testcontainers/testcontainers-scala) 0.40.3

## PocServiceLoader

The entry point of the application. Once we start the service it will execute this class to instantiate all the
required components such as the `KafkaProducer` that publish messages in a kafka topic and also registers the read
side processor.

# KafkaHelper

It contains the logic to start a [Producer](https://doc.akka.io/docs/alpakka-kafka/current/producer.html) and publish
a kafka message in a topic.

# PocHelloMessagePublisher

Each time an event is persisted, it will use the `KafkaHelper` to publish the message in Kafka.

## Integration Test

[PocHelloMessagePublisherTest](src/test/scala/com/gaston/hello/lagom/PocHelloMessagePublisherTest.scala) - It starts
a Kafka and PostgreSQL containers, then starts the Lagom Application using the 
[PocServiceLoader](src/main/scala/com/gaston/hello/lagom/PocServiceLoader.scala) as a Loader class where it instantiates
all the required components such as the [KafkaHelper](src/main/scala/com/gaston/hello/lagom/KafkaHelper.scala) and 
register [PocHelloMessagePublisher](src/main/scala/com/gaston/hello/lagom/PocHelloMessagePublisher.scala) as a 
[read side processor](https://www.lagomframework.com/documentation/1.6.x/scala/ReadSide.html). With the 
[ReadSideTestDriver](https://www.lagomframework.com/documentation/1.6.x/scala/api/com/lightbend/lagom/scaladsl/testkit/ReadSideTestDriver.html)
it simulates that an event was persisted in the [event journal](https://www.lagomframework.com/documentation/1.6.x/scala/UsingAkkaPersistenceTyped.html).
Doing that the read side processor will pick up that event and publish a message in a kafka topic. Then a 
[Consumer](https://doc.akka.io/docs/alpakka-kafka/current/consumer.html) will subscribe to the same topic to be able to
consume that message.
