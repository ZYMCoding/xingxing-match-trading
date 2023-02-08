package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.star.counter.bean.OrderInfo;
import com.star.counter.bean.PosiInfo;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.mapper.OrderMapper;
import com.star.counter.service.api.OrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    OrderMapper orderMapper;

    @Override
    public List<OrderInfo> getOrderListByUid(long uid) throws JsonProcessingException {
        String suid = Long.toString(uid);
        String orderS = redisStringCache.get(suid, CacheType.ORDER);
        if (StringUtils.isEmpty(orderS)) {
            //缓存中没有查到
            List<OrderInfo> orderInfos = orderMapper.queryOrderByUid(uid);
            List<OrderInfo> result = CollectionUtils.isEmpty(orderInfos) ? Lists.newArrayList() : orderInfos;
            //将持仓信息转换为json并写入缓存
            ObjectMapper objectMapper = new ObjectMapper();
            String resultJson = objectMapper.writeValueAsString(result);
            redisStringCache.cache(suid, resultJson, CacheType.ORDER);
            return orderInfos;
        } else {
            //命中缓存
            ObjectMapper objectMapper = new ObjectMapper();
            List<OrderInfo> orderInfos = objectMapper.readValue(orderS, new TypeReference<List<OrderInfo>>() {});
            return orderInfos;
        }
    }
}
