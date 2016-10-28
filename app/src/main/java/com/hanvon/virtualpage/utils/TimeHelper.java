package com.hanvon.virtualpage.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {

    /***
     * get datetime of the offset day to compare with current day
     *
     * @param offset
     *            day num of oddset
     * @return
     */
    private static void offsetDayDateTime(Calendar calendar, int offset) {
        calendar.add(Calendar.DATE, offset);
    }

    public static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = df.format(calendar.getTime());

        return dateStr;
    }

    public static String getCurrentYearTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        String dateStr = df.format(calendar.getTime());

        return dateStr;
    }

    public static String formatDateTime(Calendar source, String desFormatStr) {
        SimpleDateFormat desFormat = new SimpleDateFormat(desFormatStr);
        String dateStr = desFormat.format(source.getTime());
        return dateStr;
    }

    public static String formatDateTime(String source, String desFormatStr) {
        SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat desFormat = new SimpleDateFormat(desFormatStr);
        String dateStr;
        try {
            dateStr = desFormat.format(srcFormat.parse(source));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            Calendar calendar = Calendar.getInstance();
            dateStr = desFormat.format(calendar.getTime());
        }
        return dateStr;
    }

    public static String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        String yearStr = df.format(calendar.getTime());
        return yearStr;
    }

    public static String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM");
        String monthStr = df.format(calendar.getTime());
        return monthStr;
    }

    public static String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd");
        String dayStr = df.format(calendar.getTime());
        return dayStr;
    }

//    public static String getCurrentDayOfWeek() {
//
//        Calendar cal = Calendar.getInstance();
//        // cal.setTime(dt);
//        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
//        if (w < 0)
//            w = 0;
//        String[] sbList = BaseApplication.getWeekDays();
//        return sbList[w];
//    }

    /***
     * get index of the day in the week,0-Sunday,1-Monday and so on
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private static int getDayOfWeekIndex(String year, String month, String day) {
        Date d = new Date();
        d.setYear(Integer.valueOf(year) - 1900);
        d.setMonth(Integer.valueOf(month) - 1);
        d.setDate(Integer.valueOf(day));

        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return w;
    }

//    public static String getDayOfWeek(Calendar cal) {
//        String[] sbList = BaseApplication.getWeekDays();
//        String year = String.valueOf(cal.get(Calendar.YEAR));
//        String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
//        String day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
//        return sbList[getDayOfWeekIndex(year, month, day)];
//    }
//
//    public static String getDayOfWeek(String year, String month, String day) {
//        String[] sbList = BaseApplication.getWeekDays();
//        return sbList[getDayOfWeekIndex(year, month, day)];
//    }

    /***
     *
     * @param timeSpan
     *            index of resource {R.array.time_selector}
     * @return
     */
    public static Calendar getSearchOrigin(int timeSpan) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        switch (timeSpan) {
            case 0:// any time
                calendar.set(Calendar.YEAR, 1900);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DATE, 1);
                break;
            case 1:// today
                break;
            case 2:// yesterday
                calendar.add(Calendar.DATE, -1);
                break;
            case 3:// the day before yesterday
                calendar.add(Calendar.DATE, -2);
                break;
            case 4:// this week
                calendar.add(Calendar.DATE,
                        -(calendar.get(Calendar.DAY_OF_WEEK) - 1));
                break;
            case 5:// last week
                calendar.add(Calendar.DATE,
                        -(calendar.get(Calendar.DAY_OF_WEEK) - 1) - 7);
                break;
            case 6:// this month
                calendar.add(Calendar.DATE, -(calendar.get(Calendar.DATE) - 1));
                break;
            case 7:// last month
                calendar.add(Calendar.MONTH, -1);
                calendar.add(Calendar.DATE, -(calendar.get(Calendar.DATE) - 1));
                break;
            case 8:// earlier
                calendar.set(Calendar.YEAR, 1900);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DATE, 1);
                break;
        }
        return calendar;
    }

    public static Calendar getSearchEnd(int timeSpan) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        switch (timeSpan) {
            case 0:// any time
                break;
            case 1:// today
                break;
            case 2:// yesterday
                calendar.add(Calendar.DATE, -1);
                break;
            case 3:// the day before yesterday
                calendar.add(Calendar.DATE, -2);
                break;
            case 4:// this week
                break;
            case 5:// last week
                calendar.add(Calendar.DATE, -calendar.get(Calendar.DAY_OF_WEEK));
                break;
            case 6:// this month
                break;
            case 7:// last month
                calendar.add(Calendar.DATE, -calendar.get(Calendar.DATE));
                break;
            case 8:// earlier
                calendar.add(Calendar.MONTH, -1);
                calendar.add(Calendar.DATE, -calendar.get(Calendar.DATE));
                break;
        }
        return calendar;
    }

    public static String getMS() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String dateStr = df.format(calendar.getTime());
        return dateStr;
    }
}
