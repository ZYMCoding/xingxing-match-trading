package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.TradeInfo;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderCmd;

import java.util.List;

public interface TradeService {

    void saveTrade(int counterOid, MatchData md, OrderCmd orderCmd);

    List<TradeInfo> getTradeServiceByUid(long uid) throws JsonProcessingException;
}
