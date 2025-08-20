package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.RequestMethod;

/**
 * 默认实现请求断言
 *
 * @author zhaokai
 * @since 1.0
 */
public class DefaultRequestPredicate implements RequestPredicate {

    /**
     * 解析首行字符串（）
     *
     * @param requestMsg 请求消息
     * @return 首行字符串数组
     */
    private String[] firstLineProcess(String requestMsg) {
        String[] contents = requestMsg.split("\r\n");
        String firstLine = contents[0];
        return firstLine.split("\\s+");
    }
    @Override
    public boolean isClient(String requestMsg) {
        String[] array = firstLineProcess(requestMsg);
        String requestCommand = array[0];
        return RequestMethod.GET.toString().equals(requestCommand);
    }

    @Override
    public boolean isSourceTable(String requestMsg) {
        String[] array = firstLineProcess(requestMsg);
        String mountPoint = array[1];
        return "/".equals(mountPoint);
    }
}
