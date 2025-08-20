package com.github.zk.ntripcaster.util;

import java.io.File;

/**
 * 系统工具类
 *
 * @author zhaokai
 * @since v1.0
 */
public class SystemUtil {

    public static String ntripConfigPath() {
        return System.getProperty("user.dir") + File.separator + "ntripConfig" +
                File.separator;
    }
}
