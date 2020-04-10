package com.lh.offline.handler

import com.lh.datamode.UserVisitAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Dataset

import scala.collection.immutable

object SingleJumpHandler {
  /**
   *
   * @param userVisitActionRDD
   * @param singleJumpPageArray
   * @return
   */
  def getSingleJumpCount(userVisitActionRDD: RDD[UserVisitAction],
                         singleJumpPageArray: Array[String]): RDD[(String, Long)] = {
    // 1. 转换原始数据集  SessionToTimeAndPage
    val sessionToTimeAndPage: RDD[(String, (String, Long))] = userVisitActionRDD.map(userVisitAction =>
      (userVisitAction.session_id, (userVisitAction.date, userVisitAction.page_id)))

    // 2. 按session进行分组并排序 : res = [(1-2,1L),(2-3,1L),(1-2,1L)]
    val singleJumpPageAndOne: RDD[(String, Long)] = sessionToTimeAndPage.groupByKey().flatMap { case (session, items) =>
      // 排序
      val sortedList: List[(String, Long)] = items.toList.sortBy(_._1)

      // 获取排序后的pageIds
      val pageIds: List[Long] = sortedList.map(_._2)

      // 计算session内的单跳SessionJump
      val fromList: List[Long] = pageIds.dropRight(1)
      val toList: List[Long] = pageIds.drop(1)
      val sessionJump: List[String] = fromList.zip(toList).map { case (from, to) =>
        s"$from-$to"
      }

      // 按照指定的单跳条件进行过滤filterList
      val filterList: List[String] = sessionJump.filter(x => singleJumpPageArray.contains(x))
      // 返回
      filterList.map((_, 1L))

    }
    var singleJumpPageAndCount: RDD[(String, Long)] = singleJumpPageAndOne.reduceByKey(_ + _)
    singleJumpPageAndCount

  }

  def getSinglePageCount(userVisitActionRDD: RDD[UserVisitAction],
                         singlePageArray: Array[String]): RDD[(String, Long)] = {

    val filteredUserVisitAction: RDD[UserVisitAction] = userVisitActionRDD.filter(userVisitAction
    => singlePageArray.contains(userVisitAction
      .page_id.toString))

    // 计数
    val singlePageCount: RDD[(String, Long)] = filteredUserVisitAction.map(userVisitAction => (userVisitAction.page_id.toString, 1L))
      .reduceByKey(_ + _)

    singlePageCount

  }


}
