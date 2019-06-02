package com.jet.guardians.gatling

import java.util.Properties
import scala.util.Random
import org.apache.kafka.clients.producer._

object Producer {

  def main(args: Array[String]): Unit = {
    writeToKafka("my-test-topic");
  }

  def writeToKafka(topic: String): Unit = {
    val config: Properties  = new Properties()

    config.put("bootstrap.servers", "localhost:9092")
    config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](config)
    while (true) {
      val num = Random.nextInt()
      val record = new ProducerRecord[String, String](topic, num.toString, num.toString)
      producer.send(record)
      Thread.sleep(1000L)
    }
    producer.close()
  }

}
