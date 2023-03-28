package com.star.counter.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "counter")
@Data
public class CounterProperty {
    private short id;
    private long dataCenterId;
    private long workerId;
    private String sendIp;
    private int sendPort;
    private short gatewayId;
    private String subbusIp;
    private int subbusPort;
    private int pubPort;
}
