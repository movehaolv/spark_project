package com.lh.accu.accu

import org.apache.spark.util.AccumulatorV2

import scala.collection.mutable

class CategoryCountAccumulator extends AccumulatorV2[String, mutable.HashMap[String, Long]]{
  private var categoryCount: mutable.HashMap[String, Long] = new mutable.HashMap[String, Long]()

  override def isZero: Boolean = {
    categoryCount.isEmpty
  }

  override def copy(): AccumulatorV2[String, mutable.HashMap[String, Long]] = {
    val accumulator: CategoryCountAccumulator = new CategoryCountAccumulator()
    accumulator.value ++= categoryCount
    accumulator

  }

  override def reset(): Unit = {
    categoryCount.clear()
  }

  // 区内聚合
  override def add(k: String): Unit = {
    categoryCount(k) = categoryCount.getOrElse(k, 0L) + 1L
  }
  // 区间聚合
  override def merge(other: AccumulatorV2[String, mutable.HashMap[String, Long]]) = {

    val stringToLong: mutable.HashMap[String, Long] = categoryCount.foldLeft(other.value) { case (otherMap, (category, count)) =>
      otherMap(category) = count + otherMap.getOrElse(category, 0L)
      otherMap
    }
    categoryCount = stringToLong

  }

  override def value: mutable.HashMap[String, Long] = categoryCount

  // 需求将m1和m2字典的值相加


}


