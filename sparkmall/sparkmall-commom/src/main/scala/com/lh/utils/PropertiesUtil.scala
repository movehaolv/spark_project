package com.lh.utils

import java.io.InputStreamReader
import java.util.Properties

object PropertiesUtil {

  //测试时使用的
  def main(args: Array[String]): Unit = {
    println(222)
    val properties: Properties = PropertiesUtil.load("config.properties")

//    println(properties.getProperty("jdbc.user"))
//    println(properties)
  }

  def load(propertieName:String): Properties ={
    val prop=new Properties();
    prop.load(new InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertieName) , "UTF-8"))
    prop
  }
}
