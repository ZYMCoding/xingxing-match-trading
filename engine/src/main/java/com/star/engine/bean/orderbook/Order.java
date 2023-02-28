package com.star.engine.bean.orderbook;

import lombok.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import thirdpart.order.OrderDirection;

//只在OrderBook内部使用
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public final class Order {

    /**
     * 会员ID
     */
    private short mid;

    /**
     * 用户ID
     */
    private long uid;

    /**
     * 代码
     */
    private int code;

    /**
     * 方向
     */
    private OrderDirection direction;

    /**
     * 价格
     */
    private long price;

    /**
     * 量
     */
    private long volume;

    /**
     * 已成交量
     */
    private long tvolume;

    /**
     * 委托编号
     */
    private long oid;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 内部排序顺序
     */
    private long innerOid;

    //时间戳在此类重写的hashCode和equals方法的计算范围内
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(mid)
                .append(uid)
                .append(code)
                .append(direction)
                .append(price)
                .append(volume)
                .append(tvolume)
                .append(oid)
                //                .append(timestamp)
                .toHashCode();
    }

    /**
     * timestamp is not included into hashCode() and equals() for repeatable results
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Order order = (Order) o;

        return new EqualsBuilder()
                .append(mid, order.mid)
                .append(uid, order.uid)
                .append(code, order.code)
                .append(price, order.price)
                .append(volume, order.volume)
                .append(tvolume, order.tvolume)
                .append(oid, order.oid)
                //                .append(timestamp, order.timestamp)
                .append(direction, order.direction)
                .isEquals();
    }
}
