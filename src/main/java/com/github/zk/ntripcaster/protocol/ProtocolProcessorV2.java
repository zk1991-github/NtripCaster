package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV1Enum;
import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;
import com.github.zk.ntripcaster.util.Base64Util;

/**
 * 默认 sourceTable 处理器
 *
 * @author zhaokai
 * @since 1.0
 */
public class ProtocolProcessorV2 extends AbstractProtocolProcessor {

    @Override
    public String userAndPasswordDecode(String authorization) {
        return Base64Util.decode(authorization);
    }

    @Override
    public String buildSourceTable() {
        String sourceTableData = super.readSourceTableData();
        return new SourceTableHead
                .SourceTableBuidler(ResponseCodeV2Enum.OK)
                .defaultSourceTableBuild(sourceTableData)
                .toString();
    }

    @Override
    public String buildGetDataResponse(ResponseCodeV1Enum v1Enum, ResponseCodeV2Enum v2Enum) {
        return new NtripV2GetDataResponse
                .NtripV2GetDataResponseBuilder(v2Enum)
                .defaultResponseBuilder().toString();
    }

}
