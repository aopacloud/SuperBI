package net.aopacloud.superbi.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author: hu.dong
 * @date: 2021/11/30
 **/
public class DateUtils {

    public static int betweenDays(String date1, String date2) throws ParseException {

        date1 = date1.substring(0, 10);
        date2 = date2.substring(0, 10);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        long time1 = sdf.parse(date1).getTime();
        long time2 = sdf.parse(date2).getTime();

        Long betweenDays = (time2 - time1) / (1000 * 60 * 60 * 24);

        return Math.abs(betweenDays.intValue());
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getYesterdayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return sdf.format(calendar.getTime());
    }

    public static String transferDataFormat(String srcDateStr) {
        try {
            Date srcDate = new SimpleDateFormat("yyyyMMdd").parse(srcDateStr);
            return new SimpleDateFormat("yyyy-MM-dd").format(srcDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String dateToString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
