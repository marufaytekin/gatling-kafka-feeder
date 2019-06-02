import java.util
import java.util.Properties

import org.apache.kafka.clients.consumer._
import scala.collection.JavaConverters._

object Consumer {

  def main(args: Array[String]): Unit = {
    consumeFromKafka("bank-transactions")

  }

  def consumeFromKafka(topic: String): Unit = {
    val config: Properties = new Properties()

    config.put("bootstrap.servers", "localhost:9092")
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("auto.offset.reset", "earliest")
    config.put("group.id", "consumer-group")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](config)
    consumer.subscribe(util.Arrays.asList(topic))
    while (true) {
      val records = consumer.poll(100).asScala
      for (record  <- records) {
        println(record)
      }
      consumer.commitAsync
    }

  }
}