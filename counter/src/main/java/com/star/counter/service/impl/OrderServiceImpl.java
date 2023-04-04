package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.star.counter.bean.OrderInfo;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.gateway.MsgTrans;
import com.star.counter.mapper.BalanceMapper;
import com.star.counter.mapper.OrderMapper;
import com.star.counter.property.CounterProperty;
import com.star.counter.service.api.BalanceService;
import com.star.counter.service.api.OrderService;
import com.star.counter.service.api.PosiService;
import com.star.counter.util.IDConverter;
import com.star.counter.util.TimeformatUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import thirdpart.codec.api.BodyCodec;
import thirdpart.order.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.star.counter.consumer.MatchDataConsumer.ORDER_DATA_CACHE_ADDR;

@Log4j2
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    BalanceMapper balanceMapper;

    @Autowired
    CounterProperty counterProperty;

    @Autowired
    BalanceService balanceService;

    @Autowired
    PosiService posiService;

    @Autowired
    MsgTrans msgTrans;

    @Autowired
    BodyCodec bodyCodec;

    @Autowired
    Vertx sendVertx;

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
        redisStringCache.remove(Long.toString(orderCmd.uid), CacheType.ORDER);
        if (count > 0) {
            return Integer.parseInt(params.get("id").toString());
        } else {
            return -1;
        }
    }

    /**
     * 发送订单信息到网关同时将订单信息保存到数据库中
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
    public boolean sendOrder(long uid, short type, long timestamp, int code, byte direction, long price, long volume, byte ordertype) {
        //具有final字段的类的初始化：类上有lombok的@builder注解，该注解通过生成内部类来进行初始化
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
        } else {    //成功入库后才发送OrderCmd对象
            //立即调整资金持仓数据
            if (orderCmd.direction == OrderDirection.BUY) {
                //买则调整资金数据
                balanceService.minusBalance(uid, price * volume);
            } else if (orderCmd.direction == OrderDirection.SELL){
                //卖则调整持仓数据(建仓/修改现有持仓)
                posiService.minusPosi(uid, code, volume, price);
            } else {
                log.error("wrong direction[{}], ordercmd:{}", orderCmd.direction, orderCmd);
                return false;
            }
            //生成全局ID 组装ID [ 柜台ID 委托ID ], 委托ID即为数据库返回的主键ID
            orderCmd.oid = IDConverter.combineIntToLong(counterProperty.getId(), oid);

            //将委托存到缓存中去
            byte[] serialize = null;
            try {
                serialize = bodyCodec.serialize(orderCmd);
            } catch (Exception e) {
                log.error(e);
            }
            if (serialize == null) {
                return false;
            }
            sendVertx.eventBus().send(ORDER_DATA_CACHE_ADDR, Buffer.buffer(serialize));

            // 打包委托和发送数据(orderCmd -> commonMsg -> TCP数据流)
            msgTrans.sendOrder(orderCmd);
            log.info(orderCmd.toString());
            return true;
        }
    }

    @Override
    public void updateOrder(long uid, int counterOid, OrderStatus finalOrderStatus) {
        int status = finalOrderStatus.getCode();
        orderMapper.updateOrder(counterOid, status);
        redisStringCache.remove(Long.toString(uid), CacheType.ORDER);
    }

    @Override
    public boolean cancelOrder(int uid, int counteroid, int code) {
        final OrderCmd orderCmd = OrderCmd.builder()
                .uid(uid)
                .code(code)
                .type(CmdType.CANCEL_ORDER)
                .oid(IDConverter.combineIntToLong(counterProperty.getId(), counteroid))
                .build();

        log.info("recv cancel order :{}", orderCmd);

        msgTrans.sendOrder(orderCmd);
        return true;
    }
}
