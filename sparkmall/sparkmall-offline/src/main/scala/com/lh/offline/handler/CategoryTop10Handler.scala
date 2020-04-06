package com.lh.offline.handler

import com.alibaba.fastjson.JSONObject
import com.lh.datamode.UserVisitAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable


object CategoryTop10Handler {

  /**
   * 获得Top10的catgetory的点击数排名前10的Session
   * @param userVisitActionRDD ： 用户行为记录
   * @param results  Top10 category
   */
  def getCategoryTop10Session(userVisitActionRDD: RDD[UserVisitAction], results: List[(String,
    mutable.HashMap[String, Long])]) = {

    // 1. 获取Top10的category的id
    val categoryTop10: List[String] = results.map(_._1)

    // 2. 从原始userVisitActionRDD过滤出Top10的的userVisitAction
    val userVisitActionTop10: RDD[UserVisitAction] = userVisitActionRDD.filter { case userVisitAction => {
      categoryTop10.contains(userVisitAction.click_category_id.toString)
    }}

    // 3. 进行map结构变换  RDD[((category,session),1)]   RDD[((10, "session1", 1), (4, "session2", 1))]
    val categoryAndSessionToOne: RDD[((Long, String), Int)] = userVisitActionTop10.map(
      userVisitAction => ((userVisitAction.click_category_id, userVisitAction.session_id), 1))

    // 4. 计算每个category下session出现的次数，即点击次数 RDD[((category,session),count)]
    val categoryAndSessionToCount: RDD[((Long, String), Int)] = categoryAndSessionToOne.reduceByKey(_+_)

    // 5. 转换维度 变成 RDD[(category,(session,count))]
    val categoryToSessionAndCount: RDD[(Long, (String, Int))] = categoryAndSessionToCount.map {
      case ((category, session), count) => (category, (session, count)) }

    // 6. 按照category分组  RDD[(category, Iterable[(session,count)])]
    val categoryToSessionAndCountGroup: RDD[(Long, Iterable[(String, Int)])] =
      categoryToSessionAndCount.groupByKey()

    // 7. 获取每个category内的前10session
    val categorySessionTop10RDD: RDD[(Long, String, Int)] = categoryToSessionAndCountGroup.flatMap { case (category,
    items) =>
      val sortedList: List[(String, Int)] = items.toList.sortWith { case (left, right) =>
        left._2 > right._2
      }.take(10)
      val results: List[(Long, String, Int)] = sortedList.map { case (session, count) =>
        (category, session, count)
      }
      results
    }

    categorySessionTop10RDD

  }



  /**
   * 读取hive数据并过滤
   *
   * @param conditionObj
   * @param spark
   */
  def readAndFilterData(conditionObj: JSONObject, spark: SparkSession) :RDD[UserVisitAction] = {
    import spark.implicits._

    // 获取过滤参数
    val startDate: String = conditionObj.getString("startDate")
    val endDate: String = conditionObj.getString("endDate")
    val startAge: String = conditionObj.getString("startAge")
    val endAge: String = conditionObj.getString("endAge")

    // 构建sql
    val sql: StringBuilder = new StringBuilder("select v.* from sparkmall.user_visit_action v " +
      "join sparkmall.user_info u on v.user_id = u" +
      ".user_id where 1=1")

    // 判断过滤参数并追加条件
    if(startDate != null){
      sql.append(s" and `date`>='$startDate'")
    }
    if(endDate != null){
      sql.append(s" and `date`<='$endDate'")
    }
    if(startAge != null){
      sql.append(s" and age>=$startAge")
    }
    if(endAge != null){
      sql.append(s" and age<=$endAge")
    }

    // 转为rdd返回
    println("执行sql： " + sql.toString())
    val df: DataFrame = spark.sql(sql.toString())
    val userVisitActionRDD: RDD[UserVisitAction] = df.as[UserVisitAction].rdd
    userVisitActionRDD
  }

}
