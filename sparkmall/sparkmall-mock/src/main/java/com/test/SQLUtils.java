package com.test;

import scala.Tuple4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SQLUtils {
    public static String a = "aa";
    public static DateFormat sdfDt = new SimpleDateFormat("yyyy-MM-dd");
    public static DateFormat sdfM = new SimpleDateFormat("yyyy-MM");
    public static DateFormat sdfY = new SimpleDateFormat("yyyy");


    public static String readSQLFile(String sqlFile) throws IOException {

        String theSQL = "";
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(sqlFile));
        while ((line = bufferedReader.readLine()) != null) {
            theSQL += line;
        }

        return theSQL;

    }

    public static String convertHdfsPath(String inputHdfs){
        inputHdfs.split("");

//        "hdfs://test-algo-hadoop-01:8020/user/hive/warehouse/dm.db/dm_math_user_record";

        return "";
    }


    public static Tuple4<String, String, String, String> convertTo3PartDate(Timestamp ts) {

        // 在outputHdfs上面p拼接日期三分区m目录
        String dirDtTemp = "y=%s/m=%s/dt=%s";

        Date date = new Date(ts.getTime());

        String y = sdfY.format(date);
        String m = sdfM.format(date);
        String dt = sdfDt.format(date);

        String dirDt = String.format(dirDtTemp, y, m, dt);

        return new Tuple4<>(dirDt, y, m, dt);
    }
}
