package com.stellar.iot.mqtt.session;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MqttMessageManager {
    /**
     * QoS 2
     * 存储客户端发布的消息的messageId
     *  1. 生产者客户端发送publish报文时且QoS level > 1，存储messageId
     *  2. 生产者客户端发送publishRel报文时，删除messageId
     */
    private static final Set<Integer> CLIENT_PUBLISHER_MESSAGE_ID = new HashSet<>();

    /**
     * QoS 2
     * 存储Broker发布的messageId
     *  1. 消费者客户端发送publish rec报文时且QoS level > 1，存储messageId
     *  2. 消费者客户端发送publishComp报文时，删除messageId
     */
    private static final Set<Integer> SERVER_PUBLISHER_MESSAGE_ID = new HashSet<>();

    /**
     * QoS 1 && QoS 2
     */
    private static final Map<Integer, String> PUBLISH_MAP = new ConcurrentHashMap<>();

    public static void storeClientPublisherMessageId(Integer messageId) {
        CLIENT_PUBLISHER_MESSAGE_ID.add(messageId);
    }

    public static void removeClientPublisherMessageId(Integer messageId) {
        CLIENT_PUBLISHER_MESSAGE_ID.remove(messageId);
    }

    public static void storeServerPublisherMessage(Integer messageId) {
        SERVER_PUBLISHER_MESSAGE_ID.add(messageId);
    }

    public static void removeServerPublisherMessageId(Integer messageId) {
        SERVER_PUBLISHER_MESSAGE_ID.remove(messageId);
    }

    public static boolean relOutContains(Integer messageId) {
        return SERVER_PUBLISHER_MESSAGE_ID.contains(messageId);
    }

    public static void storePubMsg(Integer messageId, String clientId){
        PUBLISH_MAP.put(messageId, clientId);
    }

    public static void removePubMsg(Integer messageId) {
        PUBLISH_MAP.remove(messageId);
    }
}
