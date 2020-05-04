package com.lh.utils

import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

/**
 * 使用spark读取kafka的流式数据
 */

object MyKafkaUtil {

  // 1. 读取配置信息
  private val properties: Properties = PropertiesUtil.load("config.properties")

  // 2. 读取参数
  private val brokers: String = properties.getProperty("kafka.broker.list")
  private val group: String = properties.getProperty("kafka.group")

  // 3. 构建kafka参数
  val kafkaParams = Map(
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,  // 消费的kafka集群
    ConsumerConfig.GROUP_ID_CONFIG -> group,  // 消费者组
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest" // 自动获取最新的偏移量
  )

  // 创建kafka流
  def getKafkaDStream(ssc: StreamingContext, topics: Array[String]) ={
    // 获取kafka流
    val kafkaDstream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils
      .createDirectStream(ssc,
      LocationStrategies.PreferConsistent, // 自动分区
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams))
    // 返回
    kafkaDstream
  }
}











