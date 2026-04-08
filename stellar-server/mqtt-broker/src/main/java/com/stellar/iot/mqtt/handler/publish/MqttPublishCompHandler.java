package com.stellar.iot.mqtt.handler.publish;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.manager.MqttMessageManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;

public class MqttPublishCompHandler implements MqttHandler {
    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttMessageIdVariableHeader variableHeaderComp = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        int messageId = variableHeaderComp.messageId();
        String clientId = ChannelAttrUtils.getClientId(context.channel());
        MqttMessageManager.removeServerPublisherMessageId(clientId, messageId);
        MqttMessageManager.removePubMsg(messageId);
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.PUBCOMP;
    }
}
