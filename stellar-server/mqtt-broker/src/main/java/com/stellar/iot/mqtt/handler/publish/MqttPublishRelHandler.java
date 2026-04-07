package com.stellar.iot.mqtt.handler.publish;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttMessageManager;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

public class MqttPublishRelHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader variableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeader.messageId();
        System.out.printf("messageId=%d release\n", messageId);
        MqttMessageManager.removeClientPublisherMessageId(messageId);
        MqttFixedHeader mqttFixedHeader = new MqttFixedHeader(MqttMessageType.PUBCOMP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessagePubComplete = new MqttMessage(mqttFixedHeader, MqttMessageIdVariableHeader.from(messageId));
        ChannelFuture channelFuture = context.channel().writeAndFlush(mqttMessagePubComplete);
        channelFuture.addListener(f -> {
            if (!f.isSuccess()) {
                System.out.println("pub complete failed");
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PUBREL;
    }
}
