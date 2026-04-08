package com.stellar.iot.admin.boostrap;

import com.stellar.iot.mqtt.server.MqttServer;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class MqttNettyServerBootstrap implements SmartLifecycle {
    private MqttServer mqttServer;

    private boolean running = false;

    @Override
    public void start() {
        mqttServer = new MqttServer();
        mqttServer.start();
        running = true;
    }

    @Override
    public void stop() {
        if (mqttServer != null) {
            mqttServer.stop();
        }
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
