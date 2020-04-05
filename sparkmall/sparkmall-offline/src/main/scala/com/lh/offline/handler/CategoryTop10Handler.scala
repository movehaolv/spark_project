package com.lh.offline.handler

import com.alibaba.fastjson.JSONObject
import com.lh.datamode.UserVisitAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

object CategoryTop10Handler {

  /**
   * 读取hive数据并过滤
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
