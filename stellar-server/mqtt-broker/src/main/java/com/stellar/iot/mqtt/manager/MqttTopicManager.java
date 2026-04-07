package com.stellar.iot.mqtt.manager;

import com.stellar.iot.mqtt.bean.MqttTopicMessageWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MqttTopicManager {
    /**
     * topicName -> (clientId -> session)
     */
    private static final Map<String, Map<String, MqttTopicMessageWrapper>> SUBSCRIBE_TOPIC_MAP = new ConcurrentHashMap<>();

    public static void storeTopicSubscribeClient(String topicName, MqttTopicMessageWrapper mqttTopicMessageWrapper) {
        Map<String, MqttTopicMessageWrapper> clientIdSessionMap = SUBSCRIBE_TOPIC_MAP.computeIfAbsent(topicName, key -> new HashMap<>());
        clientIdSessionMap.put(mqttTopicMessageWrapper.getClientId(), mqttTopicMessageWrapper);
    }

    /**
     * 取消订阅topic
     *
     * @param topicName topicName
     * @param clientId 客户端id
     */
    public static void removeTopicSubscribeClient(String topicName, String clientId) {
        Map<String, MqttTopicMessageWrapper> clientIdSessionMap = SUBSCRIBE_TOPIC_MAP.computeIfAbsent(topicName, key -> new HashMap<>());
        clientIdSessionMap.remove(clientId);
    }

    public static Map<String, MqttTopicMessageWrapper> getTopicSubscribeClient(String topicName) {
        return SUBSCRIBE_TOPIC_MAP.get(topicName);
    }

    public static void publishClients(MqttPublishMessage msg, MqttTopicMessageWrapper wrapper) {
        try {
            final Channel channel = wrapper.getChannel();
            MqttQoS qos = MqttQoS.valueOf(Math.min(msg.fixedHeader().qosLevel().value(), wrapper.getMqttQoS().value()));
            ByteBuf sendBuf = msg.content().retainedDuplicate();
            sendBuf.resetReaderIndex();
            /*配置推送消息类型*/
            MqttFixedHeader Header = new MqttFixedHeader(MqttMessageType.PUBLISH,
                    false, qos, msg.fixedHeader().isRetain(), 0);
            /*设置topic packetId*/
            MqttPublishVariableHeader publishVariableHeader = new MqttPublishVariableHeader(
                    msg.variableHeader().topicName(), msg.variableHeader().packetId());
            /*推送消息*/
            MqttPublishMessage publishMessage = new MqttPublishMessage(Header,
                    publishVariableHeader, sendBuf);
            channel.writeAndFlush(publishMessage);

        } catch (Exception e) {
            System.out.printf("=>发送消息异常 %s, %s", msg, e.getMessage());
        }
    }

    public static List<String> matchedTopicList(String topic) {
        try {
            List<String> topicList = new ArrayList<>();
            topicList.add(topic);
            /*先处理#通配符*/
            String[] filterDup = topic.split("/");
            int[] source = new int[filterDup.length];
            String itemTopic = "";
            for (int i = 0; i < filterDup.length; i++) {
                String item = itemTopic.concat("#");
                topicList.add(item);
                itemTopic = itemTopic.concat(filterDup[i]).concat("/");
                source[i] = i;
            }
            /*处理+通配符*/
            Map<List<Integer>, Boolean> map = handle(source);
            for (List<Integer> key : map.keySet()) {
                String[] arr = Arrays.copyOf(filterDup, filterDup.length);
                for (Integer index : key) {
                    arr[index] = "+";
                }
                String newTopic = String.join("/", arr);
                topicList.add(newTopic);
            }
            return topicList;
        } catch (Exception e) {
            System.out.printf("=>查询topic异常=%s\n", e.getMessage());
            return null;
        }
    }

    public static Map<List<Integer>, Boolean> handle(int[] src) {
        int nCnt = src.length;
        int nBit = (0xFFFFFFFF >>> (32 - nCnt));
        Map<List<Integer>, Boolean> map = new HashMap<>();
        for (int i = 1; i <= nBit; i++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < nCnt; j++) {
                if ((i << (31 - j)) >> 31 == -1) {
                    list.add(j);
                }
            }
            map.put(list, true);
        }
        return map;
    }
}
