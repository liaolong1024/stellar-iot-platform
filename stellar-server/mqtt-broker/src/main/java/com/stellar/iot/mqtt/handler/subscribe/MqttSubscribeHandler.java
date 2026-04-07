package com.stellar.iot.mqtt.handler.subscribe;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttSession;
import com.stellar.iot.mqtt.session.MqttTopicManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

import java.util.ArrayList;
import java.util.List;

public class MqttSubscribeHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) mqttMessage;
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        MqttMessageIdVariableHeader variableHeader = subscribeMessage.variableHeader();

        // 获取订阅的Topic并存储
        System.out.printf("clientId=%s subscribe\n", clientId);
        List<Integer> grantedQos = new ArrayList<>();
        List<MqttTopicSubscription> topicList = subscribeMessage.payload().topicSubscriptions();
        for (MqttTopicSubscription mqttTopicSubscription : topicList) {
            String topicName = mqttTopicSubscription.topicName();
            MqttTopicManager.storeTopicSubscribeClient(topicName, new MqttSession(clientId, context.channel()));
            grantedQos.add(mqttTopicSubscription.qualityOfService().value());
        }

        // sub ack
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttSubAckPayload payload = new MqttSubAckPayload(grantedQos);
        MqttSubAckMessage mqttMessageSubAck = new MqttSubAckMessage(fixedHeader, MqttMessageIdVariableHeader.from(variableHeader.messageId()), payload);
        ChannelFuture channelFuture = context.channel().writeAndFlush(mqttMessageSubAck);
        channelFuture.addListener(f -> {
            if (!f.isSuccess()) {
                System.out.println("subscribe ack failed: " + channelFuture.cause().getMessage());
                channelFuture.channel().close();
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.SUBSCRIBE;
    }
}
