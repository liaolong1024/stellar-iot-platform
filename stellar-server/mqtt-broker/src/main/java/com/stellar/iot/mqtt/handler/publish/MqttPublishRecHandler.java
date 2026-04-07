package com.stellar.iot.mqtt.handler.publish;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttMessageManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

public class MqttPublishRecHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        MqttMessageIdVariableHeader recVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        MqttMessageManager.storeServerPublisherMessage(recVariableHeader.messageId());

        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(recVariableHeader.messageId());
        MqttMessage mqttRelMessage = new MqttMessage(fixedHeader, variableHeader);
        ChannelFuture channelFuture = context.channel().writeAndFlush(mqttRelMessage);
        channelFuture.addListener(f -> {
            if (!f.isSuccess()) {
                System.out.printf("clientId=%s pub rel failed\n", clientId);
                System.out.println("pub rel message=" + channelFuture.cause().getMessage());
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PUBREC;
    }
}
