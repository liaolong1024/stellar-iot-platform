package com.stellar.iot.mqtt.handler.subscribe;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.manager.MqttTopicManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

import java.util.ArrayList;
import java.util.List;

public class MqttUnsubscribeHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        MqttUnsubscribeMessage unsubscribeMessage = (MqttUnsubscribeMessage) mqttMessage;
        List<String> topics = unsubscribeMessage.payload().topics();
        List<Short> unsubscribeCode = new ArrayList<>();
        for (String topic : topics) {
            MqttTopicManager.removeTopicSubscribeClient(topic, clientId);
            unsubscribeCode.add((short) 0);
        }

        // unsub ack
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(unsubscribeMessage.variableHeader().messageId());
        MqttUnsubAckPayload payload = new MqttUnsubAckPayload(unsubscribeCode);
        MqttUnsubAckMessage mqttUnsubAckMessage = new MqttUnsubAckMessage(fixedHeader, variableHeader, payload);
        ChannelFuture channelFuture = context.channel().writeAndFlush(mqttUnsubAckMessage);
        channelFuture.addListener(f -> {
            if (!f.isSuccess()) {
                System.out.printf("clientId=%s unsub ack failed", clientId);
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.UNSUBSCRIBE;
    }
}
