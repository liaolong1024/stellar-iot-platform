package com.stellar.iot.mqtt.manager;

import io.netty.channel.Channel;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqttSession {
    private String clientId;

    private Channel channel;
}
