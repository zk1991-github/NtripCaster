package com.github.zk.ntripcaster.enums;

/**
 * Ntrip 1.0 版本返回类型枚举
 *
 * @author zk
 * @since 1.0
 */
public enum ResponseCodeV2Enum {
    //everything was fine
    OK(200, "OK"),
    //No or wrong authorization (see also header WWW-Authenticate)
    UNAUTHORIZED(401, "Unauthorized"),
    //Mountpoint of request not found (see 2.1.1 for Ntrip 1.0)
    NOT_FOUND(404, "Not Found"),
    //Mountpoint already in use by another NtripServer
    CONFLICT(409, "Conflict"),
    //e. g. some internal errors
    INTERNAL_SERVER_ERROR(500, "Internal Server Error "),
    //e. g. Requested function not implemented in NtripCaster
    NOT_IMPLEMENTED(501, "Not Implemented "),
    //e. g. in case of NtripCaster overload or bandwidth limitations
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    private final int code;
    private final String text;

    ResponseCodeV2Enum(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public int getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
