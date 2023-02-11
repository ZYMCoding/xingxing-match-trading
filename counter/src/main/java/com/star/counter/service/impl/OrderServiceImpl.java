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
import com.star.counter.property.CounterProperty;
import com.star.counter.service.api.OrderService;
import com.star.counter.util.TimeformatUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import thirdpart.order.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    CounterProperty counterProperty;

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

    /**
     * 保存订单信息：
     * 将订单信息解析为数据库对应字段并添加到数据库中
     * @param orderCmd 订单信息
     * @return 添加的行对应的主键id，添加失败则返回-1
     */
    @Override
    public int saveOrder(OrderCmd orderCmd) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", null);
        params.put("uid", orderCmd.uid);
        params.put("code", orderCmd.code);
        params.put("direction", orderCmd.direction.getDirection());
        params.put("type", orderCmd.orderType.getType());
        params.put("price", orderCmd.price);
        params.put("ocount", orderCmd.volume);
        params.put("tcount", 0);
        params.put("status", OrderStatus.NOT_SET.getCode());
        params.put("date", TimeformatUtil.yyyyMMdd(orderCmd.timestamp));
        params.put("time", TimeformatUtil.hhMMss(orderCmd.timestamp));
        int count = orderMapper.insertOrder(params);
        if (count > 0) {
            return Integer.parseInt(params.get("id").toString());
        } else {
            return -1;
        }
    }

    /**
     * 发送订单信息
     * @param uid 用户id
     * @param type 委托指令的类型
     * @param timestamp 时间戳
     * @param code 股票代码
     * @param direction 交易方向
     * @param price 价格
     * @param volume 量
     * @param ordertype 委托类型
     * @return 委托信息是否保存成功
     */
    public boolean sendOrder(long uid, short type, long timestamp, int code,
                             byte direction, long price, long volume, byte ordertype) {
        final OrderCmd orderCmd = OrderCmd.builder()
                .type(CmdType.of(type))
                .timestamp(timestamp)
                .mid(counterProperty.getId())
                .uid(uid)
                .code(code)
                .direction(OrderDirection.of(direction))
                .price(price)
                .volume(volume)
                .orderType(OrderType.of(ordertype))
                .build();
        //入库
        int oid = saveOrder(orderCmd);
        if (oid < 0) {
            return false;
        } else {
            //TODO 发送网关
            log.info(orderCmd);
            return true;
        }
    }
}
