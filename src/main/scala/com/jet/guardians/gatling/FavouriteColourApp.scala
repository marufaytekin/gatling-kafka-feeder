package com.jet.guardians.gatling

import java.lang
import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, KTable}
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig}


object FavouriteColourApp {

  def main(args: Array[String]): Unit = {

    println("Hello Scala!");

    val config: Properties = new Properties

    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-colour")
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass)
    config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

    val builder: KStreamBuilder = new KStreamBuilder

    val textLines: KStream[String, String] = builder.stream[String, String]("favourite-colour-input")

    val userAndColours: KStream[String, String] = textLines
      .filter((_, v: String) => v.contains(","))
      .selectKey[String]((_, v: String) => v.split(",")(0).toLowerCase())
      .mapValues[String]((v: String) => v.split(",")(1).toLowerCase())
      .filter((u: String, c: String) => List("green", "blue", "red").contains(c))

    val intTopic = "user-keys-and-colours-scala"
    userAndColours.to(intTopic)

    val usersAndColoursTable: KTable [String, String] = builder.table(intTopic)

    val favouriteColours: KTable[String, lang.Long] = usersAndColoursTable
      .groupBy((u: String, c: String) => new KeyValue[String, String](c, c))
      .count("CountsByColour")

    favouriteColours.to(Serdes.String, Serdes.Long, "favourite-colour-output-scala")
    val streams: KafkaStreams = new KafkaStreams(builder, config)
    streams.cleanUp()
    streams.start()

    System.out.println(streams.toString)

    Runtime.getRuntime.addShutdownHook(new Thread{
      override def run(): Unit = {
        streams.close()
      }
    })

  }
}
