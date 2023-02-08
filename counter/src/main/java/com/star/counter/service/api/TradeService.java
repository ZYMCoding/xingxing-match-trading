package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.TradeInfo;

import java.util.List;

public interface TradeService {

    List<TradeInfo> getTradeServiceByUid(long uid) throws JsonProcessingException;
}
