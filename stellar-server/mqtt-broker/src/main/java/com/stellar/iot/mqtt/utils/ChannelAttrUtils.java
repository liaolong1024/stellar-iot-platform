package com.stellar.iot.mqtt.utils;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class ChannelAttrUtils {
    private static final AttributeKey<Object> CLIENT_ID_KEY = AttributeKey.valueOf("clientId");

    public static void setClientId(Channel channel, String clientId) {
        channel.attr(CLIENT_ID_KEY).set(clientId);
    }

    public static String getClientId(Channel channel) {
        return (String) channel.attr(CLIENT_ID_KEY).get();
    }
}
