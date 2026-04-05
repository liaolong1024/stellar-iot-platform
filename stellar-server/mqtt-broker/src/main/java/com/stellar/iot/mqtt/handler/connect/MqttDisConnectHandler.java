package com.stellar.iot.mqtt.handler.connect;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttSessionManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;

public class MqttDisConnectHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        System.out.printf("clientId=%s disconnect\n", clientId);
        MqttSessionManager.removeSession(clientId);
        context.channel().close();
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.DISCONNECT;
    }
}
