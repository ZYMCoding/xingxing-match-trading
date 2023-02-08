package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.star.counter.bean.OrderInfo;
import com.star.counter.bean.PosiInfo;
import com.star.counter.bean.TradeInfo;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.mapper.TradeMapper;
import com.star.counter.service.api.TradeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    TradeMapper tradeMapper;

    @Override
    public List<TradeInfo> getTradeServiceByUid(long uid) throws JsonProcessingException {
        String suid = Long.toString(uid);
        String tradeS = redisStringCache.get(suid, CacheType.TRADE);
        if (StringUtils.isEmpty(tradeS)) {
            //缓存中没有查到
            List<TradeInfo> tradeInfos = tradeMapper.queryTradeByUid(uid);
            List<TradeInfo> result = CollectionUtils.isEmpty(tradeInfos) ? Lists.newArrayList() : tradeInfos;
            //将持仓信息转换为json并写入缓存
            ObjectMapper objectMapper = new ObjectMapper();
            String resultJson = objectMapper.writeValueAsString(result);
            redisStringCache.cache(suid, resultJson, CacheType.ORDER);
            return tradeInfos;
        } else {
            //命中缓存
            ObjectMapper objectMapper = new ObjectMapper();
            List<TradeInfo> tradeInfos = objectMapper.readValue(tradeS, new TypeReference<List<TradeInfo>>() {});
            return tradeInfos;
        }
    }
}
