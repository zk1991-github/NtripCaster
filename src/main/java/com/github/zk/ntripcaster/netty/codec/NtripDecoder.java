package com.github.zk.ntripcaster.netty.codec;

import com.github.zk.ntripcaster.model.NtripRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ntrip 解码器
 * <p>
 * 支持多次HTTP头解析（多次sourcetable/挂载点请求）。
 *
 * @author zhaokai
 * @since 1.0
 */
public class NtripDecoder extends ByteToMessageDecoder {

    private static final int MAX_HEADER_SIZE = 8192; // 8KB, 防止恶意攻击
    private static final byte[] HEADER_END = new byte[]{'\r', '\n', '\r', '\n'};

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (true) {
            int headerEndIndex = indexOf(in, HEADER_END);
            if (headerEndIndex == -1) {
                // 没有完整的HTTP头，跳出循环
                break;
            }
            int headerLength = headerEndIndex - in.readerIndex();
            if (headerLength > MAX_HEADER_SIZE) {
                ctx.close();
                return;
            }
            ByteBuf headerBytes = in.readBytes(headerLength);
            in.skipBytes(HEADER_END.length);
            String headerString = headerBytes.toString(StandardCharsets.US_ASCII);
            String[] lines = headerString.split("\r\n");
            if (lines.length == 0) {
                ctx.close();
                return;
            }
            String requestLine = lines[0];
            String[] parts = requestLine.split("\\s+");
            if (parts.length < 2) {
                ctx.close();
                return;
            }
            String method = parts[0];
            String authorization = null;
            String mountpoint = null;
            if ("GET".equalsIgnoreCase(method) || "POST".equalsIgnoreCase(method)) {
                String mountpointUri = parts[1];
                mountpoint = mountpointUri.startsWith("/") ? mountpointUri.substring(1) : mountpointUri;
                // 循环其他行，找到 Authorization
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];

                    if (line.startsWith("Authorization")) {
                        // 例 ： Authorization: Basic bnRyaXA6c2VjcmV0
                        authorization = line.split(":")[1].split("\\s+")[2];
                    }
                }
            } else {
                String mountpointUri = parts[2];
                mountpoint = mountpointUri.startsWith("/") ? mountpointUri.substring(1) : mountpointUri;
                authorization = parts[1];
            }

            out.add(new NtripRequest(method, mountpoint, authorization));
            // 循环继续，可能还有下一个HTTP头
        }
        // 剩余数据作为二进制流（如果有）
        int readableBytes = in.readableBytes();
        if (readableBytes > 0) {
            byte[] data = new byte[readableBytes];
            in.readBytes(data);
            out.add(data);
        }
    }

    /**
     * 在ByteBuf(haystack)中查找byte[](needle)的索引.
     * 这个实现是健壮的,能够处理TCP流分片导致的分次解码.
     * @param haystack 待搜索的ByteBuf
     * @param needle   要查找的字节数组
     * @return 找到则返回起始索引,否则返回-1.
     */
    private static int indexOf(ByteBuf haystack, byte[] needle) {
        for (int i = haystack.readerIndex(); i <= haystack.writerIndex() - needle.length; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack.getByte(i + j) != needle[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
}
