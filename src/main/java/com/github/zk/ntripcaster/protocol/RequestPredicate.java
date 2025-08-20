package com.github.zk.ntripcaster.protocol;

/**
 * 请求断言
 *
 * @author zhaokai
 * @since 1.0
 */
public interface RequestPredicate {

    /**
     * 客户端请求判断
     *
     * @param requestMsg 请求消息
     * @return 是否为客户端请求
     */
    boolean isClient(String requestMsg);

    /**
     * sourceTable 请求判断
     *
     * @param requestMsg 请求消息
     * @return 是否为 sourceTable
     */
    boolean isSourceTable(String requestMsg);
}
