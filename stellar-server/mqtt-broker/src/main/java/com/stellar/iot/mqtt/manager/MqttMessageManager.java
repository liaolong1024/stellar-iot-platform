package com.stellar.iot.mqtt.manager;

import com.stellar.iot.mqtt.bean.MqttTopicMessageWrapper;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MqttMessageManager {
    /**
     * QoS 2
     * 存储客户端发布的消息的messageId
     *  1. 生产者客户端发送publish报文时且QoS level > 1，存储messageId
     *  2. 生产者客户端发送publishRel报文时，删除messageId
     */
    private static final Map<String, Map<Integer, MqttTopicMessageWrapper>> CLIENT_PUBLISHER_MESSAGE_MAP = new ConcurrentHashMap<>();


    /**
     * QoS 2
     * 存储Broker发布的messageId
     *  1. 消费者客户端发送publish rec报文时且QoS level > 1，存储messageId
     *  2. 消费者客户端发送publishComp报文时，删除messageId
     */
    private static final Map<String, Map<Integer, MqttTopicMessageWrapper>> SERVER_PUBLISHER_MESSAGE_MAP = new ConcurrentHashMap<>();

    /**
     * QoS 1 && QoS 2
     */
    private static final Map<Integer, String> PUBLISH_MAP = new ConcurrentHashMap<>();

    /**
     * 存储客户端发布的消息
     *
     * @param message message
     * @param clientId clientId
     */
    public static void storeClientPublisherMessageId(MqttPublishMessage message, String clientId) {
        int packetId = message.variableHeader().packetId();
        MqttQoS mqttQoS = message.fixedHeader().qosLevel();
        MqttTopicMessageWrapper messageWrapper = MqttTopicMessageWrapper.builder()
                .mqttQoS(mqttQoS)
                .build();
        Map<Integer, MqttTopicMessageWrapper> messageMap = CLIENT_PUBLISHER_MESSAGE_MAP.computeIfAbsent(clientId, key -> new HashMap<>());
        messageMap.put(packetId, messageWrapper);
    }

    /**
     * 释放客户端发布的消息
     *
     * @param messageId messageId
     * @param clientId clientId
     */
    public static void removeClientPublisherMessageId(String clientId, Integer messageId) {
        Map<Integer, MqttTopicMessageWrapper> messageMap = CLIENT_PUBLISHER_MESSAGE_MAP.get(clientId);
        if (messageMap != null) {
            messageMap.remove(messageId);
        }
    }

    /**
     * 保存服务端发布的消息
     *
     * @param clientId clientId
     * @param messageId messageId
     * @param wrapper wrapper
     */
    public static void storeServerPublisherMessage(String clientId, Integer messageId, MqttTopicMessageWrapper wrapper) {
        Map<Integer, MqttTopicMessageWrapper> messageMap = SERVER_PUBLISHER_MESSAGE_MAP.computeIfAbsent(clientId, key -> new HashMap<>());
        messageMap.put(messageId, wrapper);
    }

    public static void removeServerPublisherMessageId(String clientId, Integer messageId) {
        Map<Integer, MqttTopicMessageWrapper> messageMap = SERVER_PUBLISHER_MESSAGE_MAP.get(clientId);
        if (messageMap != null) {
            messageMap.remove(messageId);
        }
    }

    public static boolean checkIfServerContainsMessage(String clientId, Integer messageId) {
        Map<Integer, MqttTopicMessageWrapper> messageMap = SERVER_PUBLISHER_MESSAGE_MAP.get(clientId);
        return messageMap != null && messageMap.containsKey(messageId);
    }

    public static void storePubMsg(Integer messageId, String clientId){
        PUBLISH_MAP.put(messageId, clientId);
    }

    public static void removePubMsg(Integer messageId) {
        PUBLISH_MAP.remove(messageId);
    }
}
