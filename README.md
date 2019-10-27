Introduction
------------

Kafka feeder reads `n` messages from a Kafka topic and builds a Gatling feeder. Then you can feed it into your scenarios.

Usage
-----

Add as a Dependency in your build.sbt as follows:

```bash
libraryDependencies += "com.maruf.gatling" % "gatling-kafka-feeder_2.12" % "0.0.1-SNAPSHOT" % Test
```

Import the package and create the feeder:

```scala
import com.maruf.gatling.Feeders._    

....

val feeder = KafkaFeeder("localhost:9092", "my-topic", "consumer-group-id", "number-of-messages").circular
```