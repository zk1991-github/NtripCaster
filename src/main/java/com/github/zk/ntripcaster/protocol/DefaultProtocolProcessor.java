package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.util.SystemUtil;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 默认 sourceTable 处理器
 *
 * @author zhaokai
 * @since 1.0
 */
@Component
public class DefaultProtocolProcessor extends AbstractProtocolProcessor {

    private final String SOURCE_TABLE_PATH = SystemUtil.ntripConfigPath() + "sourceTable.txt";

    @Override
    public String readSourceTableData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SOURCE_TABLE_PATH))) {
            // 使用StringBuilder来存储文件内容
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            // 读取文件内容并拼接到StringBuilder
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            // 将StringBuilder内容转换为字节数组
            return stringBuilder.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
