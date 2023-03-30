package com.star.counter.config;


import com.star.counter.consumer.MqttBusConsumer;
import com.star.counter.property.CounterProperty;
import io.vertx.core.Vertx;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import thirdpart.checksum.api.CheckSum;
import thirdpart.codec.api.MsgCodec;

import javax.annotation.PostConstruct;

@Log4j2
@Configuration
@Data
public class CounterConfig {

    @Autowired
    CounterProperty counterProperty;

    @Autowired
    MsgCodec msgCodec;

    @Autowired
    CheckSum checkSum;

    @Autowired
    Vertx socketVertx;

    @PostConstruct
    private void init() {
        new MqttBusConsumer(counterProperty.getSubbusIp(),
                counterProperty.getSubbusPort(),
                String.valueOf(counterProperty.getId()),
                msgCodec, checkSum, socketVertx).startUp();
    }
}
