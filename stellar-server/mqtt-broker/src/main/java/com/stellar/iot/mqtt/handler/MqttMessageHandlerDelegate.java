package com.stellar.iot.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttMessageHandlerDelegate implements MqttHandler {
    private final Map<MqttMessageType, MqttHandler> handlerMap;

    public MqttMessageHandlerDelegate(List<MqttHandler> handlers) {
        handlerMap = new HashMap<>();
        for (MqttHandler handler : handlers) {
            handlerMap.put(handler.handleType(), handler);
        }
    }

    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
        MqttHandler mqttHandler = handlerMap.get(messageType);
        if (mqttHandler != null) {
            mqttHandler.handle(context, mqttMessage);
        }
    }

    @Override
    public MqttMessageType handleType() {
        return null;
    }
}
