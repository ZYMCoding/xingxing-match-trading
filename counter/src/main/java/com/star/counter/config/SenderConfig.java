package com.star.counter.config;

import com.star.counter.property.CounterProperty;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import thirdpart.tcp.TcpDirectSender;

@Configuration
public class SenderConfig {

    @Autowired
    CounterProperty counterProperty;

    @Autowired
    Vertx sendVertx;

    @Bean
    public TcpDirectSender tcpDirectSender() {
        return new TcpDirectSender(counterProperty.getSendIp(), counterProperty.getSendPort(), sendVertx);
    }
}
