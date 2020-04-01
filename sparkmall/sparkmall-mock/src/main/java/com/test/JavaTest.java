package com.test;

import org.apache.spark.sql.catalyst.expressions.WindowFunctionType;
import scala.Tuple4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTest {

    public  static void  main(String[] args){
        String theSQL = "";
        String sql = "/Users/yixue/workDir/ML/data/spark/exercise/spark_project/sparkmall/sparkmall-mock/src/main/java/com/test/t.sql";


        try{
            theSQL = SQLUtils.readSQLFile(sql);
            System.out.println(theSQL);
            theSQL = MessageFormat.format(theSQL, "ctime","20212");
            System.out.println(theSQL);
        }catch (Exception e){
            System.out.println( e);
        }

//        System.out.println(theSQL);
        Timestamp ts = new Timestamp(new Date().getTime());
        Date date = new Date(ts.getTime());
        Tuple4<String, String, String, String> stringStringStringStringTuple4 = SQLUtils.convertTo3PartDate(ts);
//        System.out.println(stringStringStringStringTuple4);


        DateFormat sdfDt = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat sdfM = new SimpleDateFormat("yyyy-MM");
        DateFormat sdfY = new SimpleDateFormat("yyyy");


        String outputHdfs = "hdfs://test-algo-hadoop-01:8020/user/hive/warehouse/dm.db/cr_math_exam_step_log";
        Pattern p = Pattern.compile("^hdfs://.*:\\d+/(.*)$");
        Matcher matcher = p.matcher(outputHdfs);
        boolean m = matcher.matches();


        String outputDir = "/" + matcher.group(1);
        String locationSQL = String.format(" stored as parquet location '%s' ", outputDir);
        System.out.println(locationSQL);


    }

    public static void fun(){
        System.out.println(111);
    }


}

