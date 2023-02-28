package com.star.engine.bean.orderbook.api;

import com.star.engine.bean.command.RbCmd;
import com.star.engine.bean.orderbook.Order;
import com.star.engine.bean.orderbook.impl.OrderBucketImpl;
import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 该接口继承Comparable，使其具有排序的功能
 */
public interface OrderBucket extends Comparable<OrderBucket>{

    //编号生成器
    AtomicLong tidGen = new AtomicLong(0);

    /**
     * 新增订单
     * @param order 订单簿里封装的Order类
     */
    void put(Order order);

    /**
     * 移除订单
     * @param oid 订单序号
     */
    Order remove(long oid);

    /**
     * 订单撮合
     * @param volumeLeft          需要处理的订单量
     * @param triggerCmd          发送过来的委托
     * @param removeOrderCallback 回调函数，处理完成后的操作
     * @return
     */
    long match(long volumeLeft, RbCmd triggerCmd, Consumer<Order> removeOrderCallback);

    /////////////行情发布相关/////////////
    long getPrice();
    void setPrice(long price);
    long getTotalVolume();

    //////////////初始化选项//////////////
    static OrderBucket create(OrderBucketImplType type) {
        switch (type) {
            case GUDY:
                return new OrderBucketImpl();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Getter
    enum OrderBucketImplType {
        GUDY(0);

        private byte code;

        OrderBucketImplType(int code) {
            this.code = (byte) code;
        }
    }

    @Override
    default int compareTo(@NonNull OrderBucket other) {
        return Long.compare(this.getPrice(), other.getPrice());
    }
}