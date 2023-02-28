package com.star.engine.bean.orderbook.impl;

import com.star.engine.bean.command.RbCmd;
import com.star.engine.bean.orderbook.MatchEvent;
import com.star.engine.bean.orderbook.Order;
import com.star.engine.bean.orderbook.api.OrderBucket;
import lombok.Data;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import thirdpart.order.OrderStatus;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Log4j2
@ToString
@Data
public class OrderBucketImpl implements OrderBucket {

    //价格，每一个价格对应一个OrderBucket
    private long price;

    //量
    private long totalVolume = 0;

    //使用LinkedHashMap保存委托信息(key:委托序号, value:委托类)
    //LinkedHashMap内维护一个链表，可以按照顺序进行排列
    private final LinkedHashMap<Long, Order> entries = new LinkedHashMap<>();

    @Override
    public void put(Order order) {
        entries.put(order.getOid(), order);
        //该Bucket里需要加上订单委托量和成交的量（未成交的量）
        totalVolume += order.getVolume() - order.getTvolume();
    }

    @Override
    public Order remove(long oid) {
        //防止重复执行删除订单的请求
        Order order = entries.get(oid);
        if (order == null) {
            return null;
        }
        entries.remove(oid);
        //该Bucket里需要减去订单委托量和成交的量（未成交的量）
        totalVolume -= order.getVolume() - order.getTvolume();
        return order;
    }

    /**
     * 撮合核心方法并生成事件
     * @param volumeLeft          新发送订单需要处理的订单量
     * @param triggerCmd          发送过来的委托
     * @param removeOrderCallback 回调函数，处理完成后的操作
     * @return  总成交量
     */
    @Override
    public long match(long volumeLeft, RbCmd triggerCmd, Consumer<Order> removeOrderCallback) {
        // S 46 --> 5 10 24
        // S 45 --> 11 20 10 20
        // B 45 --> 100
        // B 44 --> 90
        // 撮合时根据优势价格进行撮合B45会在列表中按照顺序将S45吃掉
        Iterator<Map.Entry<Long, Order>> iterator = entries.entrySet().iterator();

        //收集OrderBucket中成交的量
        long volumeMatch = 0;

        // 对此Bucket里的数据进行遍历
        while (iterator.hasNext() && volumeLeft > 0) {
            Map.Entry<Long, Order> next = iterator.next();
            Order order = next.getValue();
            //计算Order可以吃掉多少量(剩余量和该委托未成交量的最小值)
            long traded = Math.min(volumeLeft, order.getVolume() - order.getTvolume());
            volumeMatch += traded;
            order.setTvolume(order.getTvolume() + traded);   //设置订单的已成交量
            volumeLeft -= traded;   //剩余量减小
            totalVolume -= traded;  //总量减小

            //生成成交事件并加入到triggerCmd的EventList中
            boolean fullMatch = order.getVolume() == order.getTvolume();   //判断是否完全成交
            genMatchEvent(order, triggerCmd, fullMatch, volumeLeft == 0, traded);

            if(fullMatch) {
                removeOrderCallback.accept(order);
                //使用迭代器遍历才可以删除元素
                iterator.remove();
            }
        }
        return volumeMatch;
    }

    private void genMatchEvent(final Order order, final RbCmd cmd, boolean fullMatch, boolean cmdFullMatch, long traded) {

        long now = System.currentTimeMillis();
        //生成成交编号后tid++
        long tid = OrderBucket.tidGen.getAndIncrement();

        //新订单生成的事件
        MatchEvent bidEvent = new MatchEvent();
        bidEvent.timestamp = now;
        bidEvent.mid = cmd.mid;    //会员号
        bidEvent.oid = cmd.oid;    //委托编号
        bidEvent.status = cmdFullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        bidEvent.tid = tid;
        bidEvent.volume = traded;
        bidEvent.price = order.getPrice();
        cmd.matchEventList.add(bidEvent);

        //被撮合(原有订单)生成的事件
        MatchEvent ofrEvent = new MatchEvent();
        ofrEvent.timestamp = now;
        ofrEvent.mid = order.getMid();    //会员号
        ofrEvent.oid = order.getOid();    //委托编号
        ofrEvent.status = fullMatch ? OrderStatus.TRADE_ED : OrderStatus.PART_TRADE;
        ofrEvent.tid = tid;
        ofrEvent.volume = order.getVolume();
        ofrEvent.price = order.getPrice();
        cmd.matchEventList.add(ofrEvent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof OrderBucketImpl)) return false;

        OrderBucketImpl that = (OrderBucketImpl) o;

        return new EqualsBuilder().append(price, that.price).append(totalVolume, that.totalVolume).append(entries, that.entries).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(price).append(totalVolume).append(entries).toHashCode();
    }
}
