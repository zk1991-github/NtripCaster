package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;
import com.github.zk.ntripcaster.util.TimeUtil;

/**
 * Ntrip 2.0 获取数据响应结构
 *
 * @author zhaokai
 * @since 1.0
 */
public class NtripV2GetDataResponse {
    /**
     * 第一行，协议版本 状态码 状态值
     */
    private final String firstLine;

    /**
     * ntrip 版本
     */
    private final String ntripVersion;

    /**
     * 服务提供方
     */
    private final String server;
    /**
     * 时间
     */
    private final String date;

    /**
     * 连接状态
     */
    private final String connection;

    private NtripV2GetDataResponse(NtripV2GetDataResponseBuilder builder) {
        this.firstLine = builder.firstLine;
        this.ntripVersion = builder.ntripVersion;
        this.server = builder.server;
        this.date = builder.date;
        this.connection = builder.connection;
    }

    public static class NtripV2GetDataResponseBuilder {
        /**
         * 第一行，协议版本 状态码 状态值
         */
        private String firstLine;

        /**
         * ntrip 版本
         */
        private String ntripVersion;

        /**
         * 服务提供方
         */
        private String server;
        /**
         * 时间
         */
        private String date;

        /**
         * 连接状态
         */
        private String connection;

        public NtripV2GetDataResponseBuilder(ResponseCodeV2Enum v2Enum) {
            this.firstLine = String.join(" ",
                    "HTTP/1.1", String.valueOf(v2Enum.getCode()), v2Enum.getText());
        }

        public NtripV2GetDataResponseBuilder ntripVersion(String ntripVersion) {
            this.ntripVersion = ntripVersion;
            return this;
        }

        public NtripV2GetDataResponseBuilder server(String server) {
            this.server = server;
            return this;
        }

        public NtripV2GetDataResponseBuilder date(String date) {
            this.date = date;
            return this;
        }

        public NtripV2GetDataResponseBuilder connection(String connection) {
            this.connection = connection;
            return this;
        }

        public NtripV2GetDataResponse build() {
            return new NtripV2GetDataResponse(this);
        }

        public NtripV2GetDataResponse defaultResponseBuilder() {
            this.firstLine =
                    String.join(" ", "HTTP/1.1",
                            String.valueOf(ResponseCodeV2Enum.OK.getCode()),
                            ResponseCodeV2Enum.OK.getText());
            this.ntripVersion = "Ntrip/2.0";
            this.server = " NTRIP ExampleCaster/2.0";
            this.date = TimeUtil.currentTime();
            this.connection = "close";
            return build();
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        //分隔符
        String separate = "\r\n";
        stringBuilder.append(firstLine).append(separate);
        stringBuilder.append("Ntrip-Version: ").append(ntripVersion).append(separate);
        stringBuilder.append("Server: ").append(server).append(separate);
        stringBuilder.append("Date: ").append(date).append(separate);
        stringBuilder.append("Connection: ").append(connection).append(separate);
        return stringBuilder.toString();
    }

}
