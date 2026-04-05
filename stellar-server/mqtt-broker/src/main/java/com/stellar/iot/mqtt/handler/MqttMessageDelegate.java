package com.stellar.iot.mqtt.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqttMessageDelegate extends SimpleChannelInboundHandler<MqttMessage> {
    private final Map<MqttMessageType, MqttHandler> handlerMap;

    public MqttMessageDelegate(List<MqttHandler> handlers) {
        handlerMap = new HashMap<>();
        for (MqttHandler handler : handlers) {
            handlerMap.put(handler.handleType(), handler);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, MqttMessage mqttMessage) {
        MqttMessageType messageType = mqttMessage.fixedHeader().messageType();
        MqttHandler mqttHandler = handlerMap.get(messageType);
        if (mqttHandler != null) {
            mqttHandler.handle(context, mqttMessage);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            IdleState state = idleStateEvent.state();
            if (state == IdleState.ALL_IDLE || state == IdleState.READER_IDLE || state == IdleState.WRITER_IDLE) {
                System.out.println("心跳超时关闭连接");
                /*关闭通道*/
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
