package com.lh.offline.app

import java.util.{Properties, UUID}

import com.lh.utils.{JdbcUtil, PropertiesUtil}
import com.alibaba.fastjson.{JSON, JSONObject}
import com.lh.accu.accu.CategoryCountAccumulator
import com.lh.datamode.UserVisitAction
import com.lh.offline.handler.CategoryTop10Handler
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

object CategoryTop10 {
  def main(args: Array[String]): Unit = {


    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("CategoryTop10")
    val spark: SparkSession = SparkSession.builder().master("local[*]").appName("CategoryTop10").enableHiveSupport().getOrCreate()

    // 将配置文件变为JSON
    val conditionPro: Properties = PropertiesUtil.load("conditions.properties")
    val conditionJson: String = conditionPro.getProperty("condition.params.json")

    // 获取Top10品类
    // JSON转为对象
    val conditionObj: JSONObject = JSON.parseObject(conditionJson)

    // 读取hive并过滤
    val userVisitActionRDD: RDD[UserVisitAction] = CategoryTop10Handler.readAndFilterData(conditionObj, spark)

    userVisitActionRDD.cache()  // 因为用了两次

    // 创建累加器
    val accumulator: CategoryCountAccumulator = new CategoryCountAccumulator

    // 注册累加器
    spark.sparkContext.register(accumulator, "categoryCount")

    // 应用累加器求三种数据的次数（点击，下单，支付） Map(click_4 -> 1898, pay_4 -> 348, order_1 -> 588)
    userVisitActionRDD.foreach{ x =>
      if(x.click_category_id != -1){
        accumulator.add("click_" + x.click_category_id)
      }else if(x.order_category_ids != null) {
        x.order_category_ids.split(",").foreach(ele => accumulator.add("order_" + ele))
      }else if(x.pay_category_ids != null) {
        x.pay_category_ids.split(",").foreach(ele=>accumulator.add("pay_" + ele))
      }
    }
//    println(accumulator.value)

    // 将accumulator.value: HashMap[order_1 -> 599,...] 按照category分组 =>  Map(1 -> Map(order_1 ->588), 4 -> Map(click_4 ->1898, pay_4 -> 348))
    val categoryGrouped: Map[String, mutable.HashMap[String, Long]] = accumulator.value.groupBy { case (k, v) => k.split("_")(1) }

    // 按照点击，下单，支付优先级排序 List((1, Map(order_1 -> 588)),(4, Map(click_4 -> 1898, pay_4 -> 348)) )
    val results: List[(String, mutable.HashMap[String, Long])] = categoryGrouped.toList.sortWith { case (c1, c2) => {
      val category1 = c1._1
      val category1Count: mutable.HashMap[String, Long] = c1._2

      val category2 = c2._1
      val category2Count: mutable.HashMap[String, Long] = c2._2

      if (category1Count.getOrElse("click_" + category1, 0L) > category2Count.getOrElse
      ("click_" + category2, 0L)) { // 按照点击降序
        true //
      }
      else if (category1Count.getOrElse("click_" + category1, 0L) == category2Count.getOrElse
      ("click_" + category2, 0L)) {
        if (category1Count.getOrElse("order_" + category1, 0L) > category2Count.getOrElse
        ("order_" + category2, 0L)) { // 点击数相同，按订单数降序
          true
        }
        else if (category1Count.getOrElse("order_" + category1, 0L) == category2Count.getOrElse
        ("order_" + category2, 0L)) {
          if (category1Count.getOrElse("pay_" + category1, 0L) > category2Count.getOrElse
          ("pay_" + category2, 0L)) { // 如果订单数相同，则按付款数降序
            true
          }
          else {
            false
          }
        }

        else {
          false
        }
      }
      else {
        false
      }

    }}.take(10)

    // 生成mysql数据
    val taskID: String = UUID.randomUUID().toString

    val categoryCountTop10Array: List[Array[Any]] = results.map { case (category, categoryCount)
    => {
      Array(taskID, category, categoryCount.getOrElse(s"click_$category", 0L), categoryCount.getOrElse
      (s"order_$category", 0L), categoryCount.getOrElse(s"pay_$category", 0L))
    }}

    // save to mysql
    JdbcUtil.executeBatchUpdate("insert into category_top10 values(?,?,?,?,?)", categoryCountTop10Array)


    // 需求二：获取Top10品类的Top10Session

    // 获取Top10前10Session
    val categorySessionTop10: RDD[(Long, String, Int)] = CategoryTop10Handler
      .getCategoryTop10Session(userVisitActionRDD, results)

    // 拉取到driver端写库
    val categorySessionTop10Array: Array[(Long, String, Int)] = categorySessionTop10.collect()

    // 写库
    val arraysSql: Array[Array[Any]] = categorySessionTop10Array.map { case (category, session, count) =>
      Array(taskID, category, session, count)
    }

    JdbcUtil.executeBatchUpdate("insert into category_session_top10 values(?,?,?,?)",arraysSql)

    spark.close()
  }
}


case class P(name:String)
