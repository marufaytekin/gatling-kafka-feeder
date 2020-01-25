Introduction
------------

Kafka feeder reads `n` messages from a Kafka topic and builds a Gatling feeder. Then you can feed it into your scenarios.

Usage
-----

Add as a Dependency as follows.

build.sbt:
```bash
libraryDependencies += "com.github.maruf" % "gatling-kafka-feeder" % "0.1" % Test
```

pom.xml:
```bash
<dependency>
    <groupId>com.github.maruf</groupId>
    <artifactId>gatling-kafka-feeder</artifactId>
    <version>0.1</version>
</dependency>
```

Import the package and create the feeder:

```scala
import com.github.maruf.Feeders._

// ....

val feeder = KafkaFeeder("localhost:9092", "my-topic", "consumer-group-01", 100, "earliest").circular
```