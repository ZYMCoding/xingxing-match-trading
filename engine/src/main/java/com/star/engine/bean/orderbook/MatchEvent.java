package com.star.engine.bean.orderbook;

import lombok.NoArgsConstructor;
import lombok.ToString;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderStatus;

@NoArgsConstructor
@ToString
public final class MatchEvent {

    public long timestamp;

    public short mid;

    public long oid;

    public OrderStatus status = OrderStatus.NOT_SET;

    public long tid;

    //撤单数量 成交数量
    public long volume;

    public long price;

    //将MatchData与MatchEvent区分开，MatchData可以直接发送到总线上去
    public MatchData copy() {
        return MatchData.builder()
                .timestamp(this.timestamp)
                .mid(this.mid)
                .oid(this.oid)
                .status(this.status)
                .tid(this.tid)
                .volume(this.volume)
                .price(this.price)
                .build();

    }
}
