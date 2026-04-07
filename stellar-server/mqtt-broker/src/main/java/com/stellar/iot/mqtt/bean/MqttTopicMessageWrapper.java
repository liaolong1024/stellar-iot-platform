package com.stellar.iot.mqtt.bean;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MqttTopicMessageWrapper {
    private String clientId;

    private MqttQoS mqttQoS;

    private Channel channel;
}
