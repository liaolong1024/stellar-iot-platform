package com.stellar.iot.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

public interface MqttHandler {
    void handle(ChannelHandlerContext context, MqttMessage mqttMessage);

    MqttMessageType handleType();
}
