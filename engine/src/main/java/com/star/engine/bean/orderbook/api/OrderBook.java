package com.star.engine.bean.orderbook.api;

import com.star.engine.bean.command.CmdResultCode;
import com.star.engine.bean.command.RbCmd;
import com.star.engine.bean.orderbook.impl.OrderBookImpl;
import lombok.Getter;
import thirdpart.hq.L1MarketData;

import static thirdpart.hq.L1MarketData.L1_SIZE;

public interface OrderBook {

    //新增委托
    CmdResultCode newOrder(RbCmd cmd);

    //撤单
    CmdResultCode cancelOrder(RbCmd rbCmd);

    //查询行情快照
    default L1MarketData getL1MarketDataSnapshot() {
        final int buySize = limitBuyBucketSize(L1_SIZE);
        final int sellSize = limitSellBucketSize(L1_SIZE);
        final L1MarketData data = new L1MarketData(buySize, sellSize);
        fillBuys(buySize, data);
        fillSells(sellSize, data);
        fillCode(data);

        data.timestamp = System.currentTimeMillis();

        return data;
    }

    void fillCode(L1MarketData data);

    void fillSells(int size, L1MarketData data);

    void fillBuys(int size, L1MarketData data);

    int limitBuyBucketSize(int maxSize);

    int limitSellBucketSize(int maxSize);

    static OrderBook create(OrderBookImplType type) {
        switch (type) {
            case GUDY:
                return new OrderBookImpl();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Getter
    enum OrderBookImplType {
        GUDY(0);

        private byte code;

        OrderBookImplType(int code) {
            this.code = (byte) code;
        }
    }
}
