package com.github.zk.ntripcaster.netty.handler;

import com.github.zk.ntripcaster.enums.RequestMethod;
import com.github.zk.ntripcaster.protocol.DefaultRequestPredicate;
import com.github.zk.ntripcaster.protocol.DefaultSourceTableProcessor;
import com.github.zk.ntripcaster.protocol.RequestPredicate;
import com.github.zk.ntripcaster.protocol.SourceTableProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ntrip Client 处理器
 *
 * @author zhaokai
 * @since 1.0
 */
public class NtripClientHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(NtripClientHandler.class);

    private RequestPredicate predicate = new DefaultRequestPredicate();

    private SourceTableProcessor sourceTableProcessor = new DefaultSourceTableProcessor();

    public void setPredicate(RequestPredicate predicate) {
        this.predicate = predicate;
    }

    public void setSourceTableProcessor(SourceTableProcessor sourceTableProcessor) {
        this.sourceTableProcessor = sourceTableProcessor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        logger.info("NtripClientHandler---" + channelId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断是否为 client
        if (predicate.isClient((String) msg)) {
            // 判断是否为 sourceTable 请求
            if (predicate.isSourceTable((String) msg)) {
                String sourceTable = sourceTableProcessor.bulidSourceTable();
                ctx.writeAndFlush(sourceTable);
            } else {
                // 挂载点请求

            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
