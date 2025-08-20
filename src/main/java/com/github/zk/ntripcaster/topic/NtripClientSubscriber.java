package com.github.zk.ntripcaster.topic;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NtripClient的订阅者实现
 * <p>
 * 每个实例与一个NtripClient的Channel绑定,负责将从发布者(Topic)收到的数据转发给该客户端.
 *
 * @author zhaokai
 * @since 1.0
 */
public class NtripClientSubscriber implements Flow.Subscriber<byte[]> {

    private final Channel channel;
    //使用原子引用来安全地持有订阅关系,确保线程安全
    private final AtomicReference<Flow.Subscription> subscriptionRef = new AtomicReference<>();

    public NtripClientSubscriber(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        //当成功订阅时会调用此方法
        //我们保存订阅关系,并请求数据
        //CAS操作,确保线程安全
        if (this.subscriptionRef.compareAndSet(null, subscription)) {
            //请求无限数据,由发布者控制速率
            subscription.request(Long.MAX_VALUE);
        } else {
            //如果已经有订阅,则取消新的订阅
            subscription.cancel();
        }
    }

    @Override
    public void onNext(byte[] item) {
        //当有新的数据时,此方法被调用
        //将数据通过绑定的channel发送给NtripClient
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.wrappedBuffer(item));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        //处理错误,例如打印日志并关闭连接
        System.err.println("Subscriber " + channel.remoteAddress() + " error: " + throwable.getMessage());
        channel.close();
    }

    @Override
    public void onComplete() {
        //当发布者关闭时,此方法被调用
        //我们也关闭客户端连接
        System.out.println("Publisher closed, closing subscriber " + channel.remoteAddress());
        channel.close();
    }

    /**
     * 当客户端断开连接时,调用此方法来取消订阅
     */
    public void cancel() {
        Flow.Subscription subscription = subscriptionRef.getAndSet(null);
        if (subscription != null) {
            subscription.cancel();
        }
    }
} 