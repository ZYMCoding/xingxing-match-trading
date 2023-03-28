package com.star.counter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.OrderInfo;
import com.star.counter.bean.PosiInfo;
import com.star.counter.bean.StockInfo;
import com.star.counter.bean.TradeInfo;
import com.star.counter.bean.res.CounterRes;
import com.star.counter.cache.StockCache;
import com.star.counter.service.api.BalanceService;
import com.star.counter.service.api.OrderService;
import com.star.counter.service.api.PosiService;
import com.star.counter.service.api.TradeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private PosiService posiService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private StockCache stockCache;

    @RequestMapping("/balance")
    @ResponseBody
    public CounterRes balanceQuery(@RequestParam long uid) throws JsonProcessingException {
        Long balance = balanceService.getBalanceByUid(uid);
        return new CounterRes(balance);
    }

    @RequestMapping("/posiinfo")
    @ResponseBody
    public CounterRes posiQuery(@RequestParam long uid) throws JsonProcessingException {
        List<PosiInfo> posiInfos = posiService.getPosiListByUid(uid);
        return new CounterRes(posiInfos);
    }

    @RequestMapping("/orderinfo")
    @ResponseBody
    public CounterRes orderQuery(@RequestParam long uid) throws JsonProcessingException {
        List<OrderInfo> orderInfos = orderService.getOrderListByUid(uid);
        return new CounterRes(orderInfos);
    }

    @RequestMapping("/tradeinfo")
    @ResponseBody
    public CounterRes tradeQuery(@RequestParam long uid) throws JsonProcessingException {
        List<TradeInfo> tradeInfos = tradeService.getTradeServiceByUid(uid);
        return new CounterRes(tradeInfos);
    }

    @RequestMapping("/code")
    @ResponseBody
    public CounterRes stockQuery(@RequestParam String key) {
        List<StockInfo> stocks = stockCache.getStocksByKey(key);
        return new CounterRes(stocks);
    }

    @RequestMapping("/sendorder")
    @ResponseBody
    public CounterRes order(@RequestParam long uid,
                            @RequestParam short type,
                            @RequestParam long timestamp,
                            @RequestParam int code,
                            @RequestParam byte direction,
                            @RequestParam long price,
                            @RequestParam long volume,
                            @RequestParam byte ordertype) {
        if (orderService.sendOrder(uid, type, timestamp, code, direction, price, volume, ordertype)) {
            return new CounterRes(CounterRes.SUCCESS, "save success", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "save failed", null);
        }
    }

    @RequestMapping("/cancelorder")
    @ResponseBody
    public CounterRes cancelOrder(@RequestParam int uid,
                                  @RequestParam int counteroid,
                                  @RequestParam int code) {
        if (orderService.cancelOrder(uid, counteroid, code)) {
            return new CounterRes(CounterRes.SUCCESS, "success", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "failed", null);
        }
    }
}
