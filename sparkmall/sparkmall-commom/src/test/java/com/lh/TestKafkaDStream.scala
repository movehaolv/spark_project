package com.lh

import java.util.Properties

import com.lh.utils.{MyKafkaUtil, PropertiesUtil}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object TestKafkaDStream {

  def main(args: Array[String]): Unit = {
    // 1

    val sparkConf: SparkConf = new SparkConf().setAppName("TestKafkaDStream").setMaster("local[*]")

    // 2
    var count = 0

    val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(3))

    // 3. 获取topic信息
    val properties: Properties = PropertiesUtil.load("config.properties")
    val topic: String = properties.getProperty("kafka.topic")

    // 4. 获取流
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaDStream(ssc, Array(topic))

    // 5. 转换并打印
    kafkaDStream.map(record => {
      count = count +  1
      val log: String = record.value()
      log
    }).print()
    // 6. 启动

    println("count :  " + count + " \n")

    ssc.start()


    ssc.awaitTermination()


  }
}
