package com.star.gateway.container;

import com.google.common.collect.Lists;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 单例
 */
public class OrderCmdContainer {

    //将实例加载为私有静态
    private static OrderCmdContainer ourInstance = new OrderCmdContainer();

    //私有的构造方法
    private OrderCmdContainer(){}

    //公共的getInstance方法
    public static OrderCmdContainer getInstance() {
        return ourInstance;
    }

    private final BlockingDeque<OrderCmd> deque = new LinkedBlockingDeque<>();

    public boolean cache(OrderCmd cmd) {
        return deque.offer(cmd);
    }

    public List<OrderCmd> getAll() {
        List<OrderCmd> msgList = Lists.newArrayList();
        //drainTo非阻塞并且将所有数据一次性去除并且清空
        int count = deque.drainTo(msgList);
        if (count == 0) {
            //防止返回空列表
            return null;
        } else {
            return msgList;
        }
    }

    public int size() {
        return deque.size();
    }
}
