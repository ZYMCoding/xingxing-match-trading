package com.star.counter.consumer;

import com.google.common.collect.Maps;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import thirdpart.bean.CommonMsg;
import thirdpart.checksum.api.CheckSum;
import thirdpart.codec.api.MsgCodec;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static thirdpart.bean.MsgConstants.MATCH_HQ_DATA;
import static thirdpart.bean.MsgConstants.MATCH_ORDER_DATA;

@RequiredArgsConstructor
@Log4j2
public class MqttBusConsumer {

    public static final String HQ_ADDR = "-1";

    public static final String INNER_MARKET_DATA_CACHE_ADDR = "l1_market_data_cache_addr";

    public static final String INNER_MATCH_DATA_ADDR = "match_data_addr";

    @NonNull
    private String busIp;

    @NonNull
    private int busPort;
    @NonNull
    private String recvAddr;

    @NonNull
    private MsgCodec msgCodec;

    @NonNull
    private CheckSum checkSum;

    @NonNull
    private Vertx vertx;

    public void startUp() {
        mqttConnect(vertx, busPort, busIp);
    }

    private void mqttConnect(Vertx vertx, int busPort, String busIp) {
        MqttClient mqttClient = MqttClient.create(vertx);
        Map<String, Integer> topoic = Maps.newHashMap();
        topoic.put(recvAddr, MqttQoS.AT_LEAST_ONCE.value());
        topoic.put(HQ_ADDR, MqttQoS.AT_LEAST_ONCE.value());
        mqttClient.connect(busPort, busIp, res -> {
            if (res.succeeded()) {
                log.info("connect mqtt bus succeed to {} {}", busIp, busPort);
                mqttClient.subscribe(topoic).publishHandler(h -> {
                    CommonMsg msg = msgCodec.decodeFromBuffer(h.payload());
                    if (msg.getChecksum() != checkSum.getCheckSum(msg.getBody())) {
                        return;
                    }
                    byte[] body = msg.getBody();
                    if (ArrayUtils.isNotEmpty(body)) {
                        short msgType = msg.getMsgType();
                        if (msgType == MATCH_ORDER_DATA) {
                            vertx.eventBus().send(INNER_MATCH_DATA_ADDR, Buffer.buffer(body));
                        } else if (msgType == MATCH_HQ_DATA) {
                            vertx.eventBus().send(INNER_MARKET_DATA_CACHE_ADDR, Buffer.buffer(body));
                        } else {
                            log.error("recv unknown msgType:{}", msg);
                        }
                    }
                });
            } else {
                log.error("connect mqtt bus failed");
            }
        });
        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect(vertx, busPort, busIp);
        });
    }
}
