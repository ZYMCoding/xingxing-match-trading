package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.star.counter.bean.TradeInfo;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.mapper.TradeMapper;
import com.star.counter.service.api.TradeService;
import com.star.counter.util.TimeformatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.Map;

@Service
public class TradeServiceImpl implements TradeService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    TradeMapper tradeMapper;

    @Override
    public void saveTrade(int counterOid, MatchData md, OrderCmd orderCmd) {
        if (orderCmd == null) {
            return;
        }
        int id = md.mid;
        long uid = orderCmd.uid;
        int code = orderCmd.code;
        int direction = orderCmd.direction.getDirection();
        long price = md.price;
        long tcount = md.volume;
        String date = TimeformatUtil.yyyyMMdd(md.timestamp);
        String time = TimeformatUtil.hhMMss(md.timestamp);
        tradeMapper.savaTrade(id, uid, code, direction, price, tcount, counterOid, date, time);

        //更新缓存
        redisStringCache.remove(Long.toString(orderCmd.uid), CacheType.TRADE);
    }

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
