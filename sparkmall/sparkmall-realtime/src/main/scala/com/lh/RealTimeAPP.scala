package com.lh

import bean.AdsLog
import com.lh.utils.MyKafkaUtil
import handler.BlackListHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object RealTimeAPP {

  def main(args: Array[String]): Unit = {
    // 1. 创建SparkConf
    val sparkConf: SparkConf = new SparkConf().setAppName("RealTimeAPP").setMaster("local[*]")

    // 2. 创建StreamingContext
    val ssc: StreamingContext = new StreamingContext(sparkConf, Seconds(3))

    // 3. 指定消费主题
    val topic = "ads_log"

    // 4. 读取kafka数据
    val kafkaDStream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaDStream(ssc, Array(topic))

    // 5.将ConsumerRecord变成自定义的JavaBean
    val adsLogDStream: DStream[AdsLog] = kafkaDStream.map(record => {
      // 取出其中的值
      val splits: Array[String] = record.value().split(" ")
      // 封装为样例类对象
      AdsLog(splits(0).toLong, splits(1), splits(2), splits(3), splits(4))
    })

    adsLogDStream.print()

    // 6. 查询黑名单进行过滤
//    BlackListHandler.filterDataByBlackList(ssc.sparkContext, adsLogDStream)

    // 7. 校验数据是否需要加入黑名单，如果超过100次则加入
//    BlackListHandler.checkUserToBlackList(adsLogDStream)

    //  测试数据
//    adsLogDStream.print()

    // 7. 启动
    ssc.start()
    ssc.awaitTermination()

  }
}
