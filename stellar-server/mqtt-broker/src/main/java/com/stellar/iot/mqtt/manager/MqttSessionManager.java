package com.stellar.iot.mqtt.manager;

import java.util.concurrent.ConcurrentHashMap;

public class MqttSessionManager {
    private static final ConcurrentHashMap<String, MqttSession> MQTT_SESSION_MAP = new ConcurrentHashMap<>();

    public static void storeSession(String clientId, MqttSession session) {
        MQTT_SESSION_MAP.put(clientId, session);
    }

    public static void removeSession(String clientId) {
        MQTT_SESSION_MAP.remove(clientId);
    }
}
