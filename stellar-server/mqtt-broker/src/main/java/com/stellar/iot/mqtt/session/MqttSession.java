package com.stellar.iot.mqtt.session;

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
