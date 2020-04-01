
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}




object Spark_Sql {
  def main(args: Array[String]): Unit = {

    val sparkConf: SparkConf = new SparkConf().setAppName("Mock").setMaster("local[*]")
    val spark: SparkSession = SparkSession.builder().config(sparkConf).enableHiveSupport().getOrCreate()
    //    val spark: SparkSession = SparkSession.builder().master("local[*]").appName("Hive Exampke").enableHiveSupport().getOrCreate()
    import spark.implicits._
    val ds = spark.createDataFrame(List(("tom",12),("Aa",20))).toDF("name","age").as[Person11]
    spark.sql("use  sparkmall")
    spark.sql("show tables").show()
    ds.write.saveAsTable("sparkmall.mamm")
    spark.sql("select * from sparkmall.mamm").show


    spark.close()


  }
}

case class Person11(name:String, age:Int)

