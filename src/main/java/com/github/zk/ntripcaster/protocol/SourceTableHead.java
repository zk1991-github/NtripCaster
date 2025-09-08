package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV1Enum;
import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;
import com.github.zk.ntripcaster.util.TimeUtil;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * 响应构建
 *
 * @author zhaokai
 * @since 1.0
 */
public class SourceTableHead {
    /**
     * 第一行，协议版本 状态码 状态值
     */
    private final String firstLine;
    /**
     * Ntrip版本
     */
    private final String ntripVersion;
    /**
     * 服务
     */
    private final String server;
    /**
     * 时间
     */
    private final String date;
    /**
     * 连接
     */
    private final String connection;
    /**
     * 内容类型
     */
    private final String contentType;
    /**
     * 内容长度
     */
    private final Integer contentLength;
    /**
     * 数据
     */
    private final String data;
    /**
     * 结束描述
     */
    private final String end;

    private SourceTableHead(SourceTableBuidler buidler) {
        this.firstLine = buidler.firstLine;
        this.ntripVersion = buidler.ntripVersion;
        this.server = buidler.server;
        this.date = buidler.date;
        this.connection = buidler.connection;
        this.contentType = buidler.contentType;
        this.contentLength = buidler.contentLength;
        this.data = buidler.data;
        this.end = buidler.end;
    }

    public static class SourceTableBuidler {
        private String firstLine;

        private String ntripVersion;

        private String server;

        private String date;

        private String connection;

        private String contentType;

        private Integer contentLength;

        private String data;

        private String end;

        public SourceTableBuidler(ResponseCodeV1Enum v1Enum) {
            this.firstLine = v1Enum.getText();
        }

        public SourceTableBuidler(ResponseCodeV2Enum v2Enum) {
            this.firstLine = String.join(" ",
                    "HTTP/1.1", String.valueOf(v2Enum.getCode()), v2Enum.getText());
        }

        public SourceTableBuidler ntripVersion(String ntripVersion) {
            this.ntripVersion = ntripVersion;
            return this;
        }

        public SourceTableBuidler server(String server) {
            this.server = server;
            return this;
        }

        public SourceTableBuidler date() {
            this.date = TimeUtil.currentTime();
            return this;
        }

        public SourceTableBuidler connection(String connection) {
            this.connection = connection;
            return this;
        }

        public SourceTableBuidler contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public SourceTableBuidler contentLength(Integer contentLength) {
            this.contentLength = contentLength;
            return this;
        }
        public SourceTableBuidler data(String data) {
            this.data = data;
            return this;
        }

        public SourceTableBuidler end() {
            this.end = "ENDSOURCETABLE";
            return this;
        }

        public SourceTableHead build() {
           return new SourceTableHead(this);
        }

        public SourceTableHead defaultSourceTableBuild(String data) {
            this.firstLine =
                    String.join(" ", "HTTP/1.1",
                            String.valueOf(ResponseCodeV2Enum.OK.getCode()),
                            ResponseCodeV2Enum.OK.getText());
            this.ntripVersion = "Ntrip/2.0";
            this.server = " NTRIP ExampleCaster/2.0";
            this.date = TimeUtil.currentTime();
            this.connection = "close";
            this.contentType = "gnss/sourcetable";
            this.contentLength = data.length();
            this.data = data;
            this.end = "ENDSOURCETABLE";
            return new SourceTableHead(this);
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
        if (StringUtils.hasText(contentType)) {
            stringBuilder.append("Content-Type: ").append(contentType).append(separate);
        }
        if (!ObjectUtils.isEmpty(contentLength)) {
            stringBuilder.append("Content-Length: ").append(contentLength).append(separate);
        }
        stringBuilder.append(separate);
        stringBuilder.append(data).append(separate);
        stringBuilder.append(end);
        return stringBuilder.toString();
    }
}
