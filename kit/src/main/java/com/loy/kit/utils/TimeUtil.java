package com.loy.kit.utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public final class TimeUtil {

    private final static SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static SimpleDateFormat TIME_DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final static SimpleDateFormat TIME_WITH_MILLIS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final static SimpleDateFormat TIME_FORMAT_CUSTOM_LETTER =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /*static {
        UTC_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }*/

    public static String getDayNow() {
        return TIME_DAY_FORMAT.format(new Date());
    }

    public static String getTimeStampNow() {
        return DEFAULT_TIME_FORMAT.format(new Date());
    }

    public static String getTimeStamp(long times) {
        return DEFAULT_TIME_FORMAT.format(new Date(times));
    }

    public static Date parse(String defaultTimeStamp) throws ParseException {
        return DEFAULT_TIME_FORMAT.parse(defaultTimeStamp);
    }

    public static String getCustomTimeStamp() {
        return TIME_FORMAT_CUSTOM_LETTER.format(new Date());
    }

    public static String getCustomTimeStamp(long timeMillis) {
        return TIME_FORMAT_CUSTOM_LETTER.format(new Date(timeMillis));
    }

    public static String getTimeStampNowWithMillis() {
        return TIME_WITH_MILLIS_FORMAT.format(new Date());
    }

}
