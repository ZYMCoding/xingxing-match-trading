package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.OrderInfo;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderStatus;

import java.util.List;

public interface OrderService {

    List<OrderInfo> getOrderListByUid(long uid) throws JsonProcessingException;

    /**
     * 保存订单信息
     * @param orderCmd 订单信息
     * @return 操作状态码
     */
    int saveOrder(OrderCmd orderCmd);

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
    boolean sendOrder(long uid, short type, long timestamp, int code, byte direction, long price, long volume, byte ordertype);

    void updateOrder(long uid, int counterOid, OrderStatus finalOrderStatus);

    //撤单
    boolean cancelOrder(int uid, int counteroid, int code);
}
