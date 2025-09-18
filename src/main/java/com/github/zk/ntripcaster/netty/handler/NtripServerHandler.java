package com.github.zk.ntripcaster.netty.handler;

import com.github.zk.ntripcaster.enums.ResponseCodeV1Enum;
import com.github.zk.ntripcaster.enums.ResponseCodeV2Enum;
import com.github.zk.ntripcaster.model.NtripRequest;
import com.github.zk.ntripcaster.protocol.AbstractProtocolProcessor;
import com.github.zk.ntripcaster.protocol.ProtocolProcessorV1;
import com.github.zk.ntripcaster.protocol.ProtocolProcessorV2;
import com.github.zk.ntripcaster.topic.NtripTopicManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;

/**
 * Ntrip Caster 的核心业务处理器
 * <p>
 * 注意: 此 Handler 必须是 prototype 作用域, 因为它为每个连接保存了状态.
 *
 * @author zhaokai
 * @since 1.0
 */
@Component
@Scope("prototype")
// @ChannelHandler.Sharable // 此 Handler 有状态, 不能被共享
public class NtripServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(NtripServerHandler.class);

    private final NtripTopicManager ntripTopicManager;

    /**
     * 标记当前连接是否已经注册为一个NtripServer(数据源).
     * 这是此Handler需要是prototype作用域的核心原因.
     */
    private boolean isNtripServerRegistered = false;

    public NtripServerHandler(NtripTopicManager ntripTopicManager) {
        this.ntripTopicManager = ntripTopicManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("客户端已连接: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("客户端已断开: {}", ctx.channel().remoteAddress());
        // 通知Topic管理器,清理此channel关联的所有资源(订阅或发布)
        ntripTopicManager.onClientDisconnect(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof NtripRequest request) {
            // --- 这是来自解码器的、结构化的初始请求 ---
            handleInitialRequest(ctx, request);
        } else if (msg instanceof byte[] data) {
            // --- 这是后续的二进制数据流 ---
            handleDataStream(ctx, data);
        } else {
            // --- 未知的消息类型 ---
            logger.warn("收到未知的消息类型: {}", msg.getClass().getName());
            try {
                super.channelRead(ctx, msg);
            } catch (Exception e) {
                logger.error("处理未知消息类型时发生异常", e);
                ctx.close();
            }
        }
    }

    private void handleInitialRequest(ChannelHandlerContext ctx, NtripRequest request) {
        String method = request.method();
        String mountPoint = request.mountpoint();
        String authorization = request.authorization();

        // NtripClient (订阅者) 请求
        if ("GET".equalsIgnoreCase(method)) {
            // 当挂载点为空时,表示请求SourceTable
            if (mountPoint.isEmpty()) {
                handleSourceTableRequest(ctx);
            } else {
                AbstractProtocolProcessor protocolProcessor = new ProtocolProcessorV2();
                // 验证用户
                if (!ObjectUtils.isEmpty(authorization) && protocolProcessor.validatePassword(authorization)) {
                    handleSubscriptionRequest(ctx, mountPoint);
                } else {
                    ctx.writeAndFlush(Unpooled.copiedBuffer(ResponseCodeV1Enum.UNAUTHORIZED.getText(), StandardCharsets.UTF_8));
                }
            }
            return;
        }

        // NtripServer (数据源) 注册
        if ("SOURCE".equalsIgnoreCase(method)) {
            AbstractProtocolProcessor protocolProcessor = new ProtocolProcessorV1();
            // 验证用户
            if (!ObjectUtils.isEmpty(authorization) && protocolProcessor.validatePassword(authorization)) {
                ntripTopicManager.registerNtripServer(mountPoint, ctx.channel());
                // 标记此连接为已注册的NtripServer
                this.isNtripServerRegistered = true;
                ctx.writeAndFlush(Unpooled.copiedBuffer(ResponseCodeV1Enum.OK.getText(), StandardCharsets.UTF_8));
                logger.info("NtripServer1.0 {} 注册到主题: {}", ctx.channel().remoteAddress(), mountPoint);
            } else {
                ctx.writeAndFlush(Unpooled.copiedBuffer(ResponseCodeV1Enum.UNAUTHORIZED.getText(), StandardCharsets.UTF_8));
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            AbstractProtocolProcessor protocolProcessor = new ProtocolProcessorV2();
            if (!ObjectUtils.isEmpty(authorization) && protocolProcessor.validatePassword(authorization)) {
                ntripTopicManager.registerNtripServer(mountPoint, ctx.channel());
                // 标记此连接为已注册的NtripServer
                this.isNtripServerRegistered = true;
                ctx.writeAndFlush(Unpooled.copiedBuffer(protocolProcessor.buildGetDataResponse(null, ResponseCodeV2Enum.OK), StandardCharsets.UTF_8));
                logger.info("NtripServer2.0 {} 注册到主题: {}", ctx.channel().remoteAddress(), mountPoint);
            } else {
                ctx.writeAndFlush(Unpooled.copiedBuffer(protocolProcessor.buildGetDataResponse(null, ResponseCodeV2Enum.UNAUTHORIZED), StandardCharsets.UTF_8));
            }
        } else {
            // 未知的请求方法
            logger.warn("收到来自 {} 的未知请求方法 '{}',即将关闭连接.", ctx.channel().remoteAddress(), method);
            ctx.writeAndFlush(Unpooled.copiedBuffer("ERROR - Bad Request\r\n", StandardCharsets.UTF_8))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void handleSourceTableRequest(ChannelHandlerContext ctx) {
        logger.info("NtripClient {} 请求SourceTable", ctx.channel().remoteAddress());
        AbstractProtocolProcessor protocolProcessor = new ProtocolProcessorV1();
        String sourceTable = protocolProcessor.buildSourceTable();
        // 发送数据后关闭连接,这是SourceTable请求的典型处理方式
        ctx.writeAndFlush(Unpooled.copiedBuffer(sourceTable, StandardCharsets.UTF_8));
    }

    private void handleSubscriptionRequest(ChannelHandlerContext ctx, String mountPoint) {
        ntripTopicManager.subscribe(mountPoint, ctx.channel());
        ctx.writeAndFlush(Unpooled.copiedBuffer("ICY 200 OK\r\n\r\n", StandardCharsets.UTF_8));
        logger.info("NtripClient {} 订阅主题: {}", ctx.channel().remoteAddress(), mountPoint);
    }

    private void handleDataStream(ChannelHandlerContext ctx, byte[] data) {
        // 只有已注册的NtripServer(数据源)才能发送数据流
        if (isNtripServerRegistered) {
            String topic = ntripTopicManager.getTopicForNtripServer(ctx.channel());
            if (topic != null) {
                ntripTopicManager.publish(topic, data);
            }
        } else {
            // NtripClient或未注册的客户端在初始请求后发送了数据,这是不规范的
            logger.warn("收到来自非NtripServer {} 的意外数据流,即将关闭连接.", ctx.channel().remoteAddress());
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("连接 {} 发生异常", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

}
