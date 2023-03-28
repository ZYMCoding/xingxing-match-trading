package com.star.counter.consumer;

import com.alipay.sofa.rpc.common.utils.JSONUtils;
import com.star.counter.config.GatewayConfig;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thirdpart.codec.api.BodyCodec;
import thirdpart.hq.L1MarketData;

import javax.annotation.PostConstruct;

import static com.star.counter.config.WebSocketConfig.L1_MARKET_DATA_PREFIX;
import static com.star.counter.consumer.MqttBusConsumer.INNER_MARKET_DATA_CACHE_ADDR;

@Log4j2
@Component
public class MarketDataConsumer {

    private IntObjectHashMap<L1MarketData> l1Cache = new IntObjectHashMap<>();

    @Autowired
    BodyCodec bodyCodec;

    @Autowired
    Vertx socketVertx;

    @PostConstruct
    private void init() {
        EventBus eventBus = socketVertx.eventBus();

        //处理核心发来的请求
        eventBus.consumer(INNER_MARKET_DATA_CACHE_ADDR).handler(buffer -> {
            Buffer body = (Buffer) buffer.body();
            if (body.length() == 0) {
                return;
            }
            L1MarketData[] marketData = null;
            try {
                marketData = bodyCodec.deserialize(body.getBytes(), L1MarketData[].class);
            } catch (Exception e) {
                log.error(e);
            }
            if (ArrayUtils.isEmpty(marketData)) {
                return;
            }
            for (L1MarketData md : marketData) {
                L1MarketData data = l1Cache.get(md.code);
                if (data == null || data.timestamp < md.timestamp) {
                    l1Cache.put(md.code, md);
                } else {
                    log.error("L1MarketData is null or L1MarketData.timestamp < md.timestamp");
                }
            }
        });

        eventBus.consumer(L1_MARKET_DATA_PREFIX).handler(h -> {
            int code = Integer.parseInt(h.headers().get("code"));
            L1MarketData data = l1Cache.get(code);
            h.reply(JSONUtils.toJSONString(data));
        });
    }
}
