package com.stellar.iot.mqtt.handler.connect;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;

public class MqttPingReqHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        System.out.printf("clientId=%s ping req\n", clientId);
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttMessage mqttMessageResp = new MqttMessage(fixedHeader);
        ChannelFuture channelFuture = context.channel().writeAndFlush(mqttMessageResp);
        channelFuture.addListener((f) -> {
            if (!f.isSuccess()) {
                System.out.println("ping resp send error");
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PINGREQ;
    }
}
