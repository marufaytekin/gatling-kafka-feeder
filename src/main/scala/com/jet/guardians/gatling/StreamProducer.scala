package com.jet.guardians.gatling

import java.util.Properties
import java.time
import java.time.Instant

import com.fasterxml.jackson.databind.node._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.Random

object StreamProducer {

  def main(args: Array[String]): Unit = {

    val config: Properties = new Properties()

    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    config.setProperty(ProducerConfig.ACKS_CONFIG, "all")
    config.setProperty(ProducerConfig.RETRIES_CONFIG, "3")
    config.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1")

    config.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")

    val producer = new KafkaProducer[String, String](config)

    var i = 0

    while (true) {
      println("Producing batch: " + i)
      try {
        producer.send(newRandomTransaction ("john"))
        Thread.sleep(100)
        producer.send(newRandomTransaction("maruf"))
        Thread.sleep(100)
        producer.send(newRandomTransaction("betul"))
        Thread.sleep(100)
        i += 1
      } catch {
        case e: InterruptedException =>
          println(e.toString)
      }
    }
    producer.close()

  }

  def newRandomTransaction(name: String): ProducerRecord[String, String] = {

    //val now = time.LocalDateTime.now
    val transaction = new ObjectNode(JsonNodeFactory.instance)
    val amount =  Random.nextInt(100)
    transaction.put("name", name)
    transaction.put("amount", amount)
    transaction.put("time", Instant.ofEpochMilli(0L).toString)

    val record = new ProducerRecord[String, String]("bank-transactions", name, transaction.toString)
    record
  }

}
