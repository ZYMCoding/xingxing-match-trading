package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.OrderInfo;

import java.util.List;

public interface OrderService {

    List<OrderInfo> getOrderListByUid(long uid) throws JsonProcessingException;
}
