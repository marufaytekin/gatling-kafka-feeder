import java.util
import java.util.Properties
import scala.util.control.Breaks._

import org.apache.kafka.clients.consumer._
import scala.collection.JavaConverters._

object Consumer {

  def main(args: Array[String]): Unit = {
    val numOfMessages = 1000
    val consumerGroupId: String = "gatling-consumer"
    val data = consumeFromKafka("localhost:9092", "bank-transactions", consumerGroupId, numOfMessages)
    println(data.length)
    for (item <- data) {
      println(item)
    }
  }

  def consumeFromKafka(servers: String, topic: String, groupId: String, n: Int=0): List[Object] = {
    val config: Properties = new Properties()
    config.put("bootstrap.servers", servers)
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    config.put("auto.offset.reset", "earliest")
    config.put("group.id", groupId)

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](config)
    consumer.subscribe(util.Arrays.asList(topic))
    var data = List[Object]()

    breakable {
      while (true) {
        val records = consumer.poll(100).asScala
        for (record <- records) {
          //println(record)
          //println(record.key(), record.value())
          data = data :+ record
          if (n != 0 && data.length > n-1) {
            consumer.commitAsync
            break()
          }
        }
        consumer.commitAsync
      }
    }
    data
  }
}