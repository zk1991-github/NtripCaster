package com.github.zk.ntripcaster.listener;

import com.github.zk.ntripcaster.netty.NettyServer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Spring Boot 启动监听器
 *
 * @author zhaokai
 * @since 1.0
 */
@Component
public class SpringBootStartListener implements ApplicationListener<ApplicationReadyEvent> {

    private final NettyServer nettyServer;

    public SpringBootStartListener(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 使用单线程的Executor来在后台启动Netty Server
        // 避免阻塞主线程
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "netty-starter-thread"));
        executor.execute(nettyServer::startUp);
        executor.shutdown();
    }
}
