package com.lh.offline.app

import java.util.{Properties, UUID}

import com.lh.offline.handler.SingleJumpHandler
import com.lh.utils.{JdbcUtil, PropertiesUtil}
import com.alibaba.fastjson.{JSON, JSONObject}
import com.lh.datamode.UserVisitAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SingleJumpRatioApp {

  def main(args: Array[String]): Unit = {

    // 1. 创建SparkSession
    val spark: SparkSession = SparkSession.builder().master("local[*]").appName("SingleJumpRatioApp").enableHiveSupport()
      .getOrCreate()

    // 导入隐式转换
    import spark.implicits._
    // 2. 读取配置文件
    val properties: Properties = PropertiesUtil.load("conditions.properties")

    val conditionJson: String = properties.getProperty("condition.params.json")

    val conditionObj: JSONObject = JSON.parseObject(conditionJson)

    // 3. 取出其中的目标页面跳转
    val targetPageFlow: String = conditionObj.getString("targetPageFlow")

    val targetPageFlowArray: Array[String] = targetPageFlow.split(",") // (1,2,3,4,5,6,7)

    val singlePageArray: Array[String] = targetPageFlowArray.dropRight(1)  // (1,2,3,4,5,6)

    // 4. 准备单跳所需的过滤条件
    val toPageArray: Array[String] = targetPageFlowArray.drop(1)
    val singleJumpPageArray: Array[String] = singlePageArray.zip(toPageArray).map { case (from, to) => {
      s"$from-$to"
    }}  // (1-2, 2-3, 3-4, 4-5, 5-6, 6-7)

    // 5. 读取Hive数据
    val userVisitActionRDD: RDD[UserVisitAction] = spark.sql("select * from sparkmall" +
      ".user_visit_action").as[UserVisitAction].rdd
    userVisitActionRDD.cache()

    // 6. 过滤单页数据并计算 [(1,count), (2,count)... (6,count)]
    val singlePageCount: RDD[(String, Long)] = SingleJumpHandler.getSinglePageCount(userVisitActionRDD, singlePageArray)

    // 7. 过滤单跳数据并计算 [(1-2,count),(2-3,count)...(6-7,count) ]
    val singleJumpPageAndCount: RDD[(String, Long)] = SingleJumpHandler.getSingleJumpCount(userVisitActionRDD, singleJumpPageArray)

    // 8. 拉取到Driver端处理
    val singlePageCountMap: Map[String, Long] = singlePageCount.collect().toMap
    val singleJumpPageAndCountArray: Array[(String, Long)] = singleJumpPageAndCount.collect()

    // 9. 计算跳转率
    val taskId: String = UUID.randomUUID().toString
    val results: Array[Array[Any]] = singleJumpPageAndCountArray.map { case (singleJump, count) =>
      val ratio: Double = count.toDouble / singlePageCountMap.getOrElse(singleJump.split("-")(0), 1L)
      //singlePageCountMap肯定会取到，1L只是写下，因为分母不能为0，不能写0L
      Array(taskId, singleJump, ratio)
    }

    // 10 写入mysql
    JdbcUtil.executeBatchUpdate("insert into jump_page_ratio values(?,?,?)", results)

    spark.close()
      
  }





}
