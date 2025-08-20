package com.github.zk.ntripcaster.netty;

import com.github.zk.ntripcaster.netty.codec.NtripDecoder;
import com.github.zk.ntripcaster.netty.handler.NtripServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 连接服务
 *
 * @author zhaokai
 * @since 1.0
 */
@Component
public class NettyServer {

    private final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Value("${ntrip.caster.port:2101}")
    private Integer port;

    private final ObjectProvider<NtripServerHandler> ntripServerHandlerProvider;

    public NettyServer(ObjectProvider<NtripServerHandler> ntripServerHandlerProvider) {
        this.ntripServerHandlerProvider = ntripServerHandlerProvider;
    }

    public void startUp() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new NtripDecoder());
                            pipeline.addLast(ntripServerHandlerProvider.getObject());
                        }
                    });
            logger.info("NtripCaster 启动于端口: {}", port);
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.error("NtripCaster 启动失败", e);
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
