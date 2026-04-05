package com.stellar.iot.mqtt.handler.connect;

import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.session.MqttSession;
import com.stellar.iot.mqtt.session.MqttSessionManager;
import com.stellar.iot.mqtt.utils.ChannelAttrUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;

public class MqttConnectHandler implements MqttHandler {
    private static final int MIN_KEEP_ALIVE_SECONDS = 20;

    private static final int MAX_KEEP_ALIVE_SECONDS = 60;

    @Override
    public void handle(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttConnectMessage connectMessage = (MqttConnectMessage) mqttMessage;
        String clientId = connectMessage.payload().clientIdentifier();
        System.out.printf("clientId=%s connected\n", clientId);

        // 配置心跳检测，超时则关闭连接
        int keepAliveTimeSeconds = connectMessage.variableHeader().keepAliveTimeSeconds();
        int idleSeconds;
        if (keepAliveTimeSeconds >= MIN_KEEP_ALIVE_SECONDS && keepAliveTimeSeconds <= MAX_KEEP_ALIVE_SECONDS) {
            idleSeconds = (int) (1.5 * keepAliveTimeSeconds);
        } else {
            idleSeconds = (int) (1.5 * MAX_KEEP_ALIVE_SECONDS);
        }
        context.pipeline().addFirst(new IdleStateHandler(0, 0, idleSeconds));

        // 保存会话
        MqttSession mqttSession = new MqttSession();
        mqttSession.setClientId(clientId);
        MqttSessionManager.storeSession(clientId, mqttSession);
        ChannelAttrUtils.setClientId(context.channel(), clientId);

        // 给客户端返回数据
        MqttMessage msg = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0x02),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false),
                null);
        ChannelFuture future = context.writeAndFlush(msg);
        future.addListener(f -> {
            if (!f.isSuccess()) {
                System.err.printf("=>响应设备[%s],发送消息:%s,失败原因:%s", clientId, msg, f.cause());
            }
        });
    }

    @Override
    public MqttMessageType handleType() {
        return MqttMessageType.CONNECT;
    }
}
