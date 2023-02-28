package com.star.engine.bean.orderbook.impl;

import com.google.common.collect.Lists;
import com.star.engine.bean.command.CmdResultCode;
import com.star.engine.bean.command.RbCmd;
import com.star.engine.bean.orderbook.MatchEvent;
import com.star.engine.bean.orderbook.Order;
import com.star.engine.bean.orderbook.api.OrderBook;
import com.star.engine.bean.orderbook.api.OrderBucket;
import io.netty.util.collection.LongObjectHashMap;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.hq.L1MarketData;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderStatus;

import java.util.*;

@Log4j2
@RequiredArgsConstructor
@NoArgsConstructor
public class OrderBookImpl implements OrderBook {

    //订单簿按照股票代码进行划分，每一个股票对应一个OrderBook
    @NonNull
    private int code;

    //将OrderBucket放入到Map中:key为价格
    private final NavigableMap<Long, OrderBucket> sellBuckets = new TreeMap<>();
    private final NavigableMap<Long, OrderBucket> buyBuckets = new TreeMap<>(Collections.reverseOrder());

    //订单的缓存
    private final LongObjectHashMap<Order> oidMap = new LongObjectHashMap<>();

    @Override
    public CmdResultCode newOrder(RbCmd cmd) {
        //判断是否重复
        if (oidMap.containsKey(cmd.oid)) {
            return CmdResultCode.DUPLICATE_ORDER_ID;
        }

        //生成新的Order
        //预撮合
        NavigableMap<Long, OrderBucket> subMatchBuckets = (cmd.direction == OrderDirection.SELL ? buyBuckets : sellBuckets)
                .headMap(cmd.price, true);     //找到对手盘中所有符合撮合的订单(买盘中高于卖出价格/卖盘中低于买入价格)
        long tVolume = preMatch(cmd, subMatchBuckets);
        if (tVolume == cmd.volume) {
            return CmdResultCode.SUCCESS;
        }
        final Order order = Order.builder()
                .mid(cmd.mid)
                .uid(cmd.uid)
                .code(code)
                .direction(cmd.direction)
                .price(cmd.price)
                .volume(cmd.volume)
                .tvolume(tVolume)
                .oid(cmd.oid)
                .timestamp(cmd.timestamp)
                .build();
        if (tVolume == 0) {
            //预撮合时没有任何成交则直接生成MatchEvent
            genMatchEvent(cmd, OrderStatus.ORDER_ED);
        } else {
            genMatchEvent(cmd, OrderStatus.PART_TRADE);
        }

        //将委托加入到OrderBucket中
        final OrderBucket orderBucket = (cmd.direction == OrderDirection.SELL ? sellBuckets : buyBuckets)
                .computeIfAbsent(cmd.price, p -> {   //如果buckets里，没有此价格对应的bucket则需要生成新的bucket
                    final OrderBucket b = OrderBucket.create(OrderBucket.OrderBucketImplType.GUDY);
                    b.setPrice(p);
                    return b;
                });
        //放入到Bucket中并加入缓存
        orderBucket.put(order);
        oidMap.put(cmd.oid, order);
        return CmdResultCode.SUCCESS;
    }

    private void genMatchEvent(RbCmd cmd, OrderStatus status) {
        long now = System.currentTimeMillis();
        MatchEvent matchEvent = new MatchEvent();
        matchEvent.timestamp = now;
        matchEvent.mid = cmd.mid;
        matchEvent.oid = cmd.oid;
        matchEvent.status = status;
        matchEvent.volume = 0;
        cmd.matchEventList.add(matchEvent);
    }

    //预撮合
    private long preMatch(RbCmd cmd, SortedMap<Long, OrderBucket> matchingBuckets) {
        int tVol = 0;
        if (matchingBuckets.size() == 0) {  //没有符合撮合的bucket
            return tVol;
        }
        List<Long> emptyBuckets = Lists.newArrayList();
        for (OrderBucket bucket : matchingBuckets.values()) {
            tVol += bucket.match(cmd.volume - tVol, cmd, order -> oidMap.remove(order.getOid()));
            if (bucket.getTotalVolume() == 0) {   //bucket中的委托耗尽
                emptyBuckets.add(bucket.getPrice());
            }
            if (tVol == cmd.volume) {   //新来的订单所有量被吃掉
                break;
            }
        }
        emptyBuckets.forEach(matchingBuckets :: remove);
        return tVol;
    }

    @Override
    public CmdResultCode cancelOrder(RbCmd cmd) {
        //从缓存中撤掉委托
        Order order = oidMap.get(cmd.oid);
        if (order == null) {
            return CmdResultCode.INVALID_ORDER_ID;
        }

        //从OrderBucket中移除委托
        //确定
        final NavigableMap<Long, OrderBucket> buckets = order.getDirection() == OrderDirection.SELL ? sellBuckets : buyBuckets;
        OrderBucket orderBucket = buckets.get(order.getPrice());
        orderBucket.remove(order.getOid());   //OrderBucketImpl中的remove方法实现了更新totalVolume
        //OrderBucket内没有量时便将此Bucket丢弃
        if(orderBucket.getTotalVolume() == 0) {
            buckets.remove(order.getPrice());
        }

        //发送撤单的MatchEvent
        MatchEvent cancelEvent = new MatchEvent();
        cancelEvent.timestamp = System.currentTimeMillis();
        cancelEvent.mid = order.getMid();    //会员号
        cancelEvent.oid = order.getOid();    //委托编号
        //判断是否有成交
        cancelEvent.status = order.getTvolume() == 0 ? OrderStatus.CANCEL_ED : OrderStatus.PART_CANCEL;
        cancelEvent.volume = order.getTvolume() - order.getVolume();  //得到未成交的量
        cmd.matchEventList.add(cancelEvent);

        //返回撤单成功的标志
        return CmdResultCode.SUCCESS;
    }

    @Override
    public void fillCode(L1MarketData data) {
        data.code = this.code;
    }

    @Override
    public void fillSells(int size, L1MarketData data) {
        if (size == 0) {
            data.sellSize = 0;
            return;
        }
        int index = 0;
        for (OrderBucket orderBucket : sellBuckets.values()) {
            data.sellPrices[index] = orderBucket.getPrice();
            data.sellVolumes[index] = orderBucket.getTotalVolume();
            if (++index == size) {
                break;
            }
        }
    }

    @Override
    public void fillBuys(int size, L1MarketData data) {
        if (size == 0) {   //买盘没有数据
            data.buySize = 0;
            return;
        }
        int index = 0;
        for (OrderBucket orderBucket : buyBuckets.values()) {
            data.buyPrices[index] = orderBucket.getPrice();
            data.buyVolumes[index] = orderBucket.getTotalVolume();
            if (++index == size) {
                break;
            }
        }
    }

    @Override
    public int limitBuyBucketSize(int maxSize) {
        return Math.min(maxSize, buyBuckets.size());
    }

    @Override
    public int limitSellBucketSize(int maxSize) {
        return Math.min(maxSize, buyBuckets.size());
    }
}
