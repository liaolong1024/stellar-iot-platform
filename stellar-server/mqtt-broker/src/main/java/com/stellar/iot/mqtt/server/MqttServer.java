package com.stellar.iot.mqtt.server;

import com.google.common.collect.Lists;
import com.stellar.iot.mqtt.handler.connect.MqttConnectHandler;
import com.stellar.iot.mqtt.handler.MqttHandler;
import com.stellar.iot.mqtt.handler.MqttMessageDelegate;
import com.stellar.iot.mqtt.handler.connect.MqttDisConnectHandler;
import com.stellar.iot.mqtt.handler.connect.MqttPingReqHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

import java.util.List;

public class MqttServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        List<MqttHandler> mqttConnectHandlers = Lists.newArrayList(
                new MqttConnectHandler(),
                new MqttDisConnectHandler(),
                new MqttPingReqHandler()
        );
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .group(bossGroup, workerGroup)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.pipeline()
                                .addLast("decoder", new MqttDecoder())
                                .addLast("encoder", MqttEncoder.INSTANCE)
                                .addLast(new MqttMessageDelegate(mqttConnectHandlers));
                    }
                });
        try {
            ChannelFuture channelFuture = serverBootstrap.bind(1883).syncUninterruptibly();
            channelFuture.channel().closeFuture().syncUninterruptibly();
        } finally {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }
}
