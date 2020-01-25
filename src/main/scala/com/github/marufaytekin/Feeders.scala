package com.github.marufaytekin

import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer._
import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder

import scala.collection.JavaConverters._
import scala.util.control.Breaks._


/**
 * This is a Feeder object that reads 'n' messages from a Kafka topic and creates a gatling feeder.
 *
 * @author maruf
 */

object Feeders {

  def KafkaFeeder(servers: String, topic: String, groupId: String, n: Int=100, autoOffsetReset: String="earliest"): SourceFeederBuilder[Object] = {
    val config = new Properties()

    // controls the amount of data to be processed in the polling loop.
    val maxPollRecords = if (n > 500) 500 else n

    config.put("bootstrap.servers", servers)
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("auto.offset.reset", autoOffsetReset)
    config.put("group.id", groupId)
    config.put("max.poll.records", maxPollRecords.toString)

    val consumer = new KafkaConsumer[String, String](config)
    consumer.subscribe(util.Arrays.asList(topic))

    var data =  Array[Map[String, Object]]()

    breakable {
      while (true) {
        val records = consumer.poll(10000).asScala
        for (record <- records) {
          data = data :+ Map(record.key() -> record.value())
          if (n != 0 && data.length > n-1) {
            consumer.commitAsync
            break()
          }
        }
        consumer.commitAsync
      }
    }
    array2FeederBuilder(data)
  }
}
