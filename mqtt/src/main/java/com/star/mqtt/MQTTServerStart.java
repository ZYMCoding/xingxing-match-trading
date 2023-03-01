package com.star.mqtt;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;

public class MQTTServerStart {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        MqttServer mqttServer = MqttServer.create(vertx);
        mqttServer.listen(1833, "127.0.0.1");
    }
}
