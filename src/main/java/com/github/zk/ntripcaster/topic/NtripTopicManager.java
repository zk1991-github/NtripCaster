package com.github.zk.ntripcaster.topic;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SubmissionPublisher;

/**
 * Ntrip主题(挂载点)管理器
 * <p>
 * 作为一个单例的Spring Bean,负责管理所有基于Java Flow API的发布-订阅逻辑.
 *
 * @author zhaokai
 * @since 1.0
 */
@Component
public class NtripTopicManager {

    // K: topic(mountPoint), V: 该topic的数据发布者
    private final ConcurrentHashMap<String, SubmissionPublisher<byte[]>> topics = new ConcurrentHashMap<>();
    // K: NtripServer(数据源)的Channel, V: 该NtripServer关联的topic(mountPoint)
    private final ConcurrentHashMap<Channel, String> ntripServerChannelToTopic = new ConcurrentHashMap<>();

    // K: NtripClient(订阅者)的Channel, V: 该NtripClient的所有订阅者实例列表
    private final ConcurrentHashMap<Channel, List<NtripClientSubscriber>> ntripClientChannelToSubscribers = new ConcurrentHashMap<>();

    /**
     * 为NtripClient(订阅者)订阅一个主题(挂载点)
     *
     * @param topic   挂载点名称
     * @param channel NtripClient的channel
     */
    public void subscribe(String topic, Channel channel) {
        // 获取或创建一个新的发布者
        SubmissionPublisher<byte[]> publisher = topics.computeIfAbsent(topic, k -> new SubmissionPublisher<>());

        // 创建订阅者并与channel关联
        NtripClientSubscriber subscriber = new NtripClientSubscriber(channel);
        // 获取或创建此Channel的订阅者列表,并添加新的订阅者.
        // CopyOnWriteArrayList是线程安全的.
        ntripClientChannelToSubscribers.computeIfAbsent(channel, k -> new CopyOnWriteArrayList<>()).add(subscriber);

        // 订阅
        publisher.subscribe(subscriber);
        System.out.println("NtripClient " + channel.remoteAddress() + " subscribed to topic: " + topic);
    }

    /**
     * 为NtripServer(数据源)注册其发布的主题(挂载点)
     * 首次连接时调用
     *
     * @param topic   挂载点
     * @param channel NtripServer的channel
     */
    public void registerNtripServer(String topic, Channel channel) {
        ntripServerChannelToTopic.put(channel, topic);
        System.out.println("NtripServer " + channel.remoteAddress() + " registered for topic: " + topic);
    }

    /**
     * 发布数据到指定主题
     *
     * @param topic 挂载点
     * @param data  要发布的数据
     */
    public void publish(String topic, byte[] data) {
        SubmissionPublisher<byte[]> publisher = topics.get(topic);
        if (publisher != null) {
            // 提交数据,所有订阅了该主题的Subscriber都会收到
            publisher.submit(data);
        }
    }

    /**
     * 当客户端断开连接时,进行清理
     *
     * @param channel 断开的channel
     */
    public void onClientDisconnect(Channel channel) {
        // 尝试作为NtripClient(订阅者)清理
        // 移除并获取该channel的所有订阅者
        List<NtripClientSubscriber> subscribers = ntripClientChannelToSubscribers.remove(channel);
        if (subscribers != null) {
            // 取消该channel下的所有订阅
            for (NtripClientSubscriber subscriber : subscribers) {
                subscriber.cancel();
            }
            System.out.println("NtripClient " + channel.remoteAddress() + " unsubscribed from all topics.");
            return;
        }

        // 尝试作为NtripServer(数据源)清理
        String topic = ntripServerChannelToTopic.remove(channel);
        if (topic != null) {
            System.out.println("NtripServer " + channel.remoteAddress() + " disconnected from topic: " + topic);
            // 可选: 当一个挂载点的所有NtripServer都断开时,可以关闭这个Publisher
            // 这里我们暂时不关闭,因为可能还有其他NtripServer在发布相同挂载点,或者为了NtripClient可以持续等待
            // SubmissionPublisher<byte[]> publisher = topics.get(topic);
            // if (publisher != null && !ntripServerChannelToTopic.containsValue(topic)) {
            //     publisher.close();
            //     topics.remove(topic);
            //     System.out.println("Topic " + topic + " closed due to no publishers.");
            // }
        }
    }

    /**
     * 根据NtripServer(数据源)的Channel获取其发布的主题
     *
     * @param channel NtripServer的Channel
     * @return 主题, 如果不存在则返回null
     */
    public String getTopicForNtripServer(Channel channel) {
        return ntripServerChannelToTopic.get(channel);
    }
} 