package com.star.counter.config;

import com.star.counter.property.CounterProperty;
import com.star.counter.util.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdWorkerConfig {

    @Autowired
    CounterProperty counterProperty;

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(counterProperty.getWorkerId(), counterProperty.getDataCenterId());
    }
}
