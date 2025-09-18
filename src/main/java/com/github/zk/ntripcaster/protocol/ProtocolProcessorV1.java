package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV1Enum;
import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;

/**
 * 默认 sourceTable 处理器
 *
 * @author zhaokai
 * @since 1.0
 */
public class ProtocolProcessorV1 extends AbstractProtocolProcessor {

    @Override
    public String userAndPasswordDecode(String authorization) {
        return authorization;
    }

    @Override
    public String buildSourceTable() {
        String sourceTableData = super.readSourceTableData();
        return new SourceTableHead
                .SourceTableBuidler(ResponseCodeV1Enum.SOURCETABLE)
                .defaultSourceTableBuild(sourceTableData)
                .toString();
    }

    @Override
    public String buildGetDataResponse(ResponseCodeV1Enum v1Enum, ResponseCodeV2Enum v2Enum) {
        return v1Enum.getText();
    }

}
