package com.github.zk.ntripcaster.enums;

/**
 * Ntrip 1.0 版本返回类型枚举
 *
 * @author zk
 * @since 1.0
 */
public enum ResponseCodeV1Enum {
    //正常值
    OK("ICY 200 OK\r\n"),
    //密码错误
    UNAUTHORIZED("ERROR - Bad Password\r\n"),
    //挂载点已占用
    CONFLICT("ERROR - Already Connected\r\n");

    /**
     * 返回信息
     */
    private final String text;


    ResponseCodeV1Enum(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
