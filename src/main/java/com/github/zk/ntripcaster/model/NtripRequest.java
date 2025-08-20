package com.github.zk.ntripcaster.model;

/**
 * 一个NTRIP请求的结构化表示.
 *
 *  @author zhaokai
 *  @since 1.0
 *
 * @param method     请求方法, 如 GET, SOURCE, POST.
 * @param mountpoint 请求的挂载点.
 */
public record NtripRequest(String method, String mountpoint, String authorization) {
}