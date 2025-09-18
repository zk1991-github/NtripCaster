package com.github.zk.ntripcaster.protocol;

import com.github.zk.ntripcaster.enums.ResponseCodeV1Enum;
import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;
import com.github.zk.ntripcaster.util.SystemUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * sourceTable 处理器
 * @author zhaokai
 * @since 1.0
 */
public abstract class AbstractProtocolProcessor {

    private final String SOURCE_TABLE_PATH = SystemUtil.ntripConfigPath() + "sourceTable.txt";

    public abstract String userAndPasswordDecode(String authorization);

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

    public abstract String buildSourceTable();

    /**
     * 构建获取数据响应结构
     *
     * @param v1Enum v1版本枚举
     * @param v2Enum v2版本枚举
     * @return 响应结构
     */
    public abstract String buildGetDataResponse(ResponseCodeV1Enum v1Enum, ResponseCodeV2Enum v2Enum);

    /**
     * 验证密码
     *
     * @param authorization base64编码后的密码
     * @return 密码是否正确
     */
    public Boolean validatePassword(String authorization) {
        String userAndPassword = userAndPasswordDecode(authorization);
        try {
            return Files.lines(Paths.get(SystemUtil.ntripConfigPath() + "user.txt"))
                    .anyMatch(s -> s.equals(userAndPassword));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
