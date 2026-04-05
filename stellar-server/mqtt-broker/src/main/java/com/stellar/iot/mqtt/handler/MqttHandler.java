package com.stellar.iot.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;

public interface MqttHandler {
    void handle(ChannelHandlerContext context, MqttMessage mqttMessage);
}
