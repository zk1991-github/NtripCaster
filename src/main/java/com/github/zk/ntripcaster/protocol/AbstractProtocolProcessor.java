package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;

/**
 * sourceTable 处理器
 * @author zhaokai
 * @since 1.0
 */
public abstract class AbstractProtocolProcessor {

    public abstract String readSourceTableData();

    public String bulidSourceTable() {
        String sourceTableData = readSourceTableData();
        return new SourceTableHead
                .SourceTableBuidler(ResponseCodeV2Enum.OK)
                .defaultSourceTableBuild(sourceTableData)
                .toString();
    }

    public String buildGetDataV2Response(ResponseCodeV2Enum v2Enum) {
        return new NtripV2GetDataResponse
                .NtripV2GetDataResponseBuilder(v2Enum)
                .defaultResponseBuilder().toString();
    }

    public String buildGetDataV1Response() {
        return "ICY 200 OK\r\n";
    }

}
