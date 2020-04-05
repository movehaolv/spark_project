
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.datanucleus.store.rdbms.JDBCUtils

import scala.collection.mutable.ArrayBuffer
import com.lh.utils.JdbcUtil



object Spark_Sql {
  def main(args: Array[String]): Unit = {

//    val sparkConf: SparkConf = new SparkConf().setAppName("Mock").setMaster("local[*]")
////    val spark: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()
//    //    val spark: SparkSession = SparkSession.builder().master("local[*]").appName("Hive Exampke").enableHiveSupport().getOrCreate()
//    val spark: SparkSession = SparkSession.builder().master("local[*]").appName("CategoryTop10").enableHiveSupport().getOrCreate()
//    import spark.implicits._
////    val ds = spark.createDataFrame(List(("tom",12),("Aa",20))).toDF("name","age").as[Person11]
//    println("city_info---------------------------------")
//
//    spark.sql("select * from sparkmall.user_info limit 10").show()
//    spark.close()
    JdbcUtil.dataSource


  }
}

case class Person11(name:String, age:Int)


class Spark_Sql{
  val arr = ArrayBuffer[Int]()

  def copy(): Spark_Sql ={
    val t = new Spark_Sql()
    t.value.appendAll(arr)
    t
  }

  def value = arr

}
