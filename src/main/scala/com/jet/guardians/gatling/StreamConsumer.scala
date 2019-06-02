package com.jet.guardians.gatling

import java.time.Instant
import java.util.Properties

import com.fasterxml.jackson.databind.JsonNode
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.connect.json.{JsonDeserializer, JsonSerializer}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.kstream.KStreamBuilder
import com.fasterxml.jackson.databind.node._


object StreamConsumer {

  def newBalance(transaction: JsonNode, balanceUpdate: JsonNode): JsonNode = {
    val newBalance = new ObjectNode(JsonNodeFactory.instance)
    //val newCount = (balanceUpdate.get("count").asInt + 1).toString
    newBalance.put("name", balanceUpdate.get("name"))
    newBalance.put("amount", (balanceUpdate.get("amount").asInt + transaction.get("amount").asInt()).toString)

    val balanceEpoch = Instant.parse(balanceUpdate.get("time").asText()).toEpochMilli
    val transactionEpoch = Instant.parse(transaction.get("time").asText()).toEpochMilli
    val newBalanceEpoch = Instant.ofEpochMilli(Math.max(balanceEpoch, transactionEpoch))
    newBalance.put("time", newBalanceEpoch.toString)
    newBalance
  }

  def main(args: Array[String]): Unit = {

    val config: Properties = new Properties()

    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-balance-app")
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")

    config.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")

    config.setProperty(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE)

    val jsonSerializer = new JsonSerializer
    val jsonDeserializer = new JsonDeserializer
    val jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer)

    val builder = new KStreamBuilder

    val bankTransactions = builder.stream(Serdes.String(), jsonSerde, "bank-transactions")

    val initialBalance = new ObjectNode(JsonNodeFactory.instance)

    initialBalance.put("name", "")
    initialBalance.put("amount", 0)
    initialBalance.put("time", Instant.ofEpochMilli(0L).toString)

    val bankBalance = bankTransactions
      .groupByKey(Serdes.String(), jsonSerde)
        .reduce((initialBalance:JsonNode, balanceUpdate:JsonNode) => newBalance(initialBalance, balanceUpdate));

    bankBalance.to(Serdes.String(), jsonSerde ,"bank-balance-exactly-once")

    val streams = new KafkaStreams(builder, config)
    streams.cleanUp()
    streams.start()

    println(streams.toString())

  }

}
