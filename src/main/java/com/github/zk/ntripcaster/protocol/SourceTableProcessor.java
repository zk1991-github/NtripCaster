package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;

/**
 * sourceTable 处理器
 * @author zhaokai
 * @since 1.0
 */
public abstract class SourceTableProcessor {

    public abstract String readSourceTableData();

    public String bulidSourceTable() {
        String sourceTableData = readSourceTableData();
        return new SourceTableHead
                .SourceTableBuidler(ResponseCodeV2Enum.OK)
                .defaultSourceTableBuild(sourceTableData)
                .toString();
    }

}
