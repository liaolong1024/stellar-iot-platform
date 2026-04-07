package com.stellar.iot.mqtt.handler.publish;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttMessageManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;

public class MqttPublishAckHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttPubAckMessage pubAckMessage = (MqttPubAckMessage) mqttMessage;
        MqttMessageManager.removePubMsg(pubAckMessage.variableHeader().messageId());
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PUBACK;
    }
}
