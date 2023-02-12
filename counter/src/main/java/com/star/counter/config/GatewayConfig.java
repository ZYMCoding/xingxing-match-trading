package com.star.counter.config;

import com.star.counter.property.CounterProperty;
import io.vertx.core.Vertx;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thirdpart.checksum.api.CheckSum;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.api.BodyCodec;
import thirdpart.codec.api.MsgCodec;
import thirdpart.codec.impl.BodyCodecImpl;
import thirdpart.codec.impl.MsgCodecImpl;
import thirdpart.tcp.TcpDirectSender;

@Log4j2
@Configuration
public class GatewayConfig {

    @Autowired
    CounterProperty counterProperty;

    @Bean
    public CheckSum checkSum() {
        return new CheckSumImpl();
    }

    @Bean
    public BodyCodec bodyCodec() {
        return new BodyCodecImpl();
    }

    @Bean
    public MsgCodec msgCodec() {
        return new MsgCodecImpl();
    }

    private final Vertx vertx = Vertx.vertx();

    @Bean
    public TcpDirectSender tcpDirectSender() {
        return new TcpDirectSender(counterProperty.getSendIp(), counterProperty.getSendPort(), vertx);
    }
}