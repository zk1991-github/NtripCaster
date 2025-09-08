package com.github.zk.ntripcaster.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author zhaokai
 * @since 1.0
 */
public class TimeUtil {

    /**
     * 当前时间
     *
     * @return 当前时间字符串
     */
    public static String currentTime() {
        // 获取当前的日期和时间
        ZonedDateTime dateTime = ZonedDateTime.now(ZoneOffset.UTC);

        // 创建一个DateTimeFormatter实例，设置日期格式
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.CHINA)
                        .withZone(ZoneOffset.UTC);

        // 格式化当前时间
        String formattedDate = formatter.format(dateTime);

        // 替换Z为GMT
        formattedDate = formattedDate.replace("Z", "GMT");
        return formattedDate;
    }
}
