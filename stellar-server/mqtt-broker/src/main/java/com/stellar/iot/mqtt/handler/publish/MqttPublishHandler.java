package com.stellar.iot.mqtt.handler.publish;

import com.stellar.iot.mqtt.bean.MqttTopicMessageWrapper;
import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.manager.MqttMessageManager;
import com.stellar.iot.mqtt.manager.MqttTopicManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

public class MqttPublishHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        System.out.printf("clientId=%s publish\n", clientId);
        MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
        String topicName = publishMessage.variableHeader().topicName();
        System.out.printf("clientId=%s, topicName=%s\n", clientId, topicName);

        // 告知客户端已收到消息
        sendPublishResult2Client(context, publishMessage, clientId);

        // 推送消息给订阅了该topic的客户端
        pushMessage2Client(publishMessage);


    }

    private void pushMessage2Client(MqttPublishMessage message) {
        String topic = message.variableHeader().topicName();
        List<String> topicList = MqttTopicManager.matchedTopicList(topic);
        if (CollectionUtils.isEmpty(topicList)) {
            System.out.println("topicList is empty");
            return;
        }

        for (String itemTopic : topicList) {
            Map<String, MqttTopicMessageWrapper> clientMap = MqttTopicManager.getTopicSubscribeClient(itemTopic);
            if (MapUtils.isEmpty(clientMap)) {
                continue;
            }
            for (MqttTopicMessageWrapper wrapper : clientMap.values()) {
                MqttTopicManager.publishClients(message, wrapper);
            }
        }
    }


    private void sendPublishResult2Client(ChannelHandlerContext context, MqttPublishMessage message, String clientId) {
        /*获取消息等级*/
        MqttQoS mqttQoS = message.fixedHeader().qosLevel();
        int packetId = message.variableHeader().packetId();
        System.out.printf("messageId=%d\n", packetId);
        MqttFixedHeader fixedHeader;
        switch (mqttQoS.value()) {
            /*0,1消息等级，直接回复*/
            case 0:
            case 1:
                fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, mqttQoS, false, 0);
                break;
            case 2:
                // 处理Qos2的消息确认
                if (!MqttMessageManager.checkIfServerContainsMessage(clientId, packetId)) {
                    MqttMessageManager.storeClientPublisherMessageId(message, clientId);
                }
                fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREC, false, MqttQoS.AT_MOST_ONCE, false, 0);
                break;
            default:
                fixedHeader = null;
        }
        /*处理消息等级*/
        if (mqttQoS == MqttQoS.AT_LEAST_ONCE || mqttQoS == MqttQoS.EXACTLY_ONCE) {
            MqttMessageManager.storePubMsg(packetId, clientId);
        }
        /*响应客户端*/
        MqttMessageIdVariableHeader variableHeader = null;
        if (packetId > 0) {
            variableHeader = MqttMessageIdVariableHeader.from(packetId);
        }
        MqttPubAckMessage ackMessage = new MqttPubAckMessage(fixedHeader, variableHeader);
        if (mqttQoS.value() >= 1) {
            ChannelFuture channelFuture = context.channel().writeAndFlush(ackMessage);
            channelFuture.addListener(f -> {
                if (!f.isSuccess()) {
                    System.out.println("mqtt publish ack failed, clientId=" + clientId);
                }
           });
        }
    }
    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PUBLISH;
    }
}
