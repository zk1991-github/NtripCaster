package com.github.zk.ntripcaster.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * base64 加解密
 *
 * @author zhaokai
 * @since v1.0
 */
public class Base64Util {

    /**
     * base64 解码
     *
     * @param encodedString 编码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
