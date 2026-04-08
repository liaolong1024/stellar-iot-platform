package com.stellar.iot.mqtt.server;

import com.google.common.collect.Lists;
import com.stellar.iot.mqtt.handler.MqttHandlerInboundAdapter;
import com.stellar.iot.mqtt.handler.connect.MqttConnectHandler;
import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.handler.MqttMessageHandlerDelegate;
import com.stellar.iot.mqtt.handler.connect.MqttDisConnectHandler;
import com.stellar.iot.mqtt.handler.connect.MqttPingReqHandler;
import com.stellar.iot.mqtt.handler.publish.*;
import com.stellar.iot.mqtt.handler.subscribe.MqttSubscribeHandler;
import com.stellar.iot.mqtt.handler.subscribe.MqttUnsubscribeHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

import java.util.List;

public class MqttServer {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    public void start() {
        ServerBootstrap serverBootStrap = createServerBootStrap();
        ChannelFuture channelFuture;
        try {
            channelFuture = serverBootStrap.bind(1883).sync();
            if (!channelFuture.isSuccess()) {
                throw new RuntimeException("Netty 启动失败", channelFuture.cause());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Netty 启动被中断", e);
        }
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        System.out.println("mqtt server stopped");
    }

    private ServerBootstrap createServerBootStrap() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);
        List<MqttHandler> mqttConnectHandlers = Lists.newArrayList(
                new MqttConnectHandler(),
                new MqttDisConnectHandler(),
                new MqttPingReqHandler(),
                new MqttPublishHandler(),
                new MqttPublishRelHandler(),
                new MqttSubscribeHandler(),
                new MqttUnsubscribeHandler(),
                new MqttPublishRecHandler(),
                new MqttPublishCompHandler(),
                new MqttPublishAckHandler()
        );
        MqttMessageHandlerDelegate delegate = new MqttMessageHandlerDelegate(mqttConnectHandlers);
        return new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(bossGroup, workerGroup)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
                                .addLast("decoder", new MqttDecoder())
                                .addLast("encoder", MqttEncoder.INSTANCE)
                                .addLast(new MqttHandlerInboundAdapter(delegate));
                    }
                });

    }
}
