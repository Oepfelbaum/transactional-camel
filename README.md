# transactional-camel
A small prototype connecting Apache Kafka and JMS Queues transactionally via Apache Camel

## Principle

This project is a demonstration how different messaging platforms can communicate transactionally with Apache Camel as middleware. i.e. no message is lost in case of crashes, offline servers or erroneous messages. It is able to do that by following 2 principles:

1. Messages are not consumed from its source until it has been successfully transmitted and processed.
2. Erroneous Messages that can't be successfully processed are sent back to the sender inside a *Dead Letter Queue/Topic*

This demo with Apache Camel is able to read messages from either an IBM MQ Queue or Apache Kafka Topic and either route them to a REST endpoint or to a different Message Queue.

## Components

### Apache Camel via Spring Boot

The integration framework. Most of the configuration that makes the communication transactional is made in Apache Camel.

### IBM MQ

IBM implementation of a Message Broker with Message Queues. With the URI parameter "transacted=true", Apache Camel will wait until the Exchange has concluded successfully before it consumes the message.

### Apache Kafka

Event streaming platform with Topics. Topic entries are not removed on consumption. An integer Offset is moved to mark which message have been consumed already.

By default, Kafka will update its Offsets every 5 seconds to the current position, regardless if messages have successfully reached their destination. By disabling this mechanism (with Camel URI parameter *allowManualCommit=true* and *autoCommitEnable=false*) and manually updating the Offset, it can be ensured that every message behind the Offset is correctly processed.

### A very simple REST server with Node.js

REST server with a single POST endpoint */api/messageSink* that responds with a 400 error code if the request body contains the string "Error" or with a 200 status code otherwise. Messages that invoke a 4xx HTTP-status code will be put back into the Dead Letter Queue/Topic.

Configured in the subfolder */node-rest-server/*.

## Run with Docker Containers

*Note: the Docker image for IBM MQ only works on x86_64 and s390x machines*

The simplest way to run this demo is with *docker-compose*. With Docker installed, run the command

~~~
docker compose up -d
~~~

And then add new messages via the web clients of
- IBM MQ: https://localhost:9443/ibmmq/console/ (User: "admin" / pw: "passw0rd")
- Apache Kafka: http://localhost:8080 (bootstrap server: "kafka:9092")
