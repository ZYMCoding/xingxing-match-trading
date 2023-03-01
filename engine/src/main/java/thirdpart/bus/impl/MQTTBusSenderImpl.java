package thirdpart.bus.impl;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;
import thirdpart.bus.api.BusSender;
import thirdpart.codec.api.MsgCodec;

import java.util.concurrent.TimeUnit;

@Log4j2
@RequiredArgsConstructor
public class MQTTBusSenderImpl implements BusSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private MsgCodec msgCodec;

    @NonNull
    private Vertx vertx;

    @Override
    public void startUp() {
        //连接总线
        mqttConnect();
    }

    private void mqttConnect() {
        MqttClient mqttClient = MqttClient.create(vertx);
        mqttClient.connect(port, ip, res -> {
            if (res.succeeded()) {
                sender = mqttClient;
                log.info("connect to mqtt bus[ip:{}, port:{}] succeed", ip, port);
            } else {
                log.info("connect to mqtt bus[ip:{}, port:{}] fail", ip, port);
                mqttConnect();
            }
        });

        mqttClient.closeHandler(h -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                log.error(e);
            }
            mqttConnect();
        });
    }

    private volatile MqttClient sender;

    @Override
    public void publish(CommonMsg commonMsg) {
        sender.publish(Short.toString(commonMsg.getMsgDst()),   //发往的柜台id
                msgCodec.encodeToBuffer(commonMsg),
                MqttQoS.AT_LEAST_ONCE,      //总线保证至少到达一次
                false,      //不判断是否重复
                false);     //不判断是否保存
    }
}
