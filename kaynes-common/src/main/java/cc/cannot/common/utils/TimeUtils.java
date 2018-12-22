package cc.cannot.dict.common.utils;

import org.joda.time.format.ISODateTimeFormat;

public class TimeUtils {
    private TimeUtils() {

    }

    /**
     * 得到当前系统时间的字符串表示，格式为RFC3339："2008-11-13T12:23:30-08:00"
     *
     * @return 当前时刻字符串
     */
    public static String getNowRFC3339() {
        return ISODateTimeFormat.dateTime().print(System.currentTimeMillis());
    }

    public static String getRFC3339FromMs(long ms) {
        return ISODateTimeFormat.dateTime().print(ms);
    }

    public static String getRFC3339FromSeconds(int seconds) {
        return getRFC3339FromMs((long) seconds * 1000);
    }

    public static Integer currentTimeSeconds() {
        Long currentTime = System.currentTimeMillis() / 1000;
        return currentTime.intValue();
    }
}
