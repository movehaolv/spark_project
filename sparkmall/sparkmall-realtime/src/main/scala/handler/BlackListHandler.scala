package handler

import java.text.SimpleDateFormat
import java.util.Date

import bean.AdsLog
import com.lh.utils.RedisUtil
import org.apache.spark.SparkContext
import org.apache.spark.streaming.dstream.DStream
import redis.clients.jedis.Jedis

object BlackListHandler {
//  def filterDataByBlackList(sparkContext: SparkContext, adsLogDStream: DStream[AdsLog]) = {
//    adsLogDStream.transform(rdd => {
//
//    })
//  }


  // 时间转换对象
  private val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  // redis保存用户点击次数的key
  val redisKey: String = "date-user-ads"

  // redis中黑名单的key
  val blackList: String = "blackList"

  /**
   * 点击数超过100（每天每个用户的点击数超过100），则加入黑名单
   * @param adsLogDStream
   * @return
   */
  def checkUserToBlackList(adsLogDStream: DStream[AdsLog]) = {
    // 1. 转换维度
    val dateUserAdsOne: DStream[(String, Long)] = adsLogDStream.map(adsLog => {

      // 获取时间并将时间转换为年月日
      val dateStr: String = sdf.format(new Date(adsLog.timestamp))

      // 拼接Key
      val key = s"$dateStr:${adsLog.userid}:${adsLog.adid}"

      // 返回
      (key, 1L)
    })

    // 2. 按照key聚合
    val dateUserAdsSum: DStream[(String, Long)] = dateUserAdsOne.reduceByKey(_ + _)

    // 3. 存入redis
    dateUserAdsSum.foreachRDD(rdd => {
      // foreachRDD 和 foreachPartition 之间的的代码是在driver端执行
      // 对每个分区进行处理，将redis的链接写在分区（因为foreachPartition是在每个executor中执行，foreachRDD是在driver中执行，这样不需要序列化）
      rdd.foreachPartition(items => {
        // 获取Jedis
        val jedis: Jedis = RedisUtil.getJedisClient

        // 读出数据进行判断是否超过100
        items.foreach{case (key, count) =>
          // 写入Redis
          jedis.hincrBy(redisKey, key, count)

          // 读出数据进行判断是否超过100
          if(jedis.hget(redisKey, key).toLong >= 50L){
            // 获取用户id
            val userId: String = key.split(":")(1)

            // 若超过，则加入黑名单
            jedis.sadd(blackList, userId)

          }
        }
        // 关闭jedis连接
        jedis.close()

      })
    })

  }

}
