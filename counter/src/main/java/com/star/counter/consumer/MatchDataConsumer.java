package com.star.counter.consumer;

import com.alipay.sofa.rpc.common.utils.JSONUtils;
import com.google.common.collect.ImmutableMap;
import com.star.counter.config.GatewayConfig;
import com.star.counter.mapper.TradeMapper;
import com.star.counter.property.CounterProperty;
import com.star.counter.service.api.BalanceService;
import com.star.counter.service.api.OrderService;
import com.star.counter.service.api.PosiService;
import com.star.counter.service.api.TradeService;
import com.star.counter.util.IDConverter;
import io.netty.util.collection.LongObjectHashMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import thirdpart.codec.api.BodyCodec;
import thirdpart.hq.MatchData;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderStatus;

import javax.annotation.PostConstruct;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.star.counter.config.WebSocketConfig.ORDER_NOTIFY_ADDR_PREFIX;
import static com.star.counter.config.WebSocketConfig.TRADE_NOTIFY_ADDR_PREFIX;
import static com.star.counter.consumer.MqttBusConsumer.INNER_MATCH_DATA_ADDR;

@Log4j2
@Component
public class MatchDataConsumer {

    @Autowired
    CounterProperty counterProperty;

    @Autowired
    BodyCodec bodyCodec;

    @Autowired
    TradeService tradeService;

    @Autowired
    PosiService posiService;

    @Autowired
    BalanceService balanceService;

    @Autowired
    OrderService orderService;

    @Autowired
    Vertx socketVertx;

    private LongObjectHashMap<OrderCmd> oidOrderMap = new LongObjectHashMap<>();

    public static final String ORDER_DATA_CACHE_ADDR = "order_data_cache_addr";

    @PostConstruct
    private void init() {
        EventBus eventBus = socketVertx.eventBus();
        eventBus.consumer(INNER_MATCH_DATA_ADDR).handler(buffer -> {
            Buffer body = (Buffer) buffer.body();
            if (body.length() == 0) {
                return;
            }
            MatchData[] matchDataArr = null;
            try {
                matchDataArr = bodyCodec.deserialize(body.getBytes(), MatchData[].class);
            } catch (Exception e) {
                log.error(e);
            }
            if (ArrayUtils.isEmpty(matchDataArr)) {
                return;
            }
            //按照oid进行分类
            Map<Long, List<MatchData>> collect = Arrays.asList(matchDataArr).stream().collect(Collectors.groupingBy(t -> t.oid));
            for (Map.Entry<Long, List<MatchData>> entry : collect.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    continue;
                }
                //拆分获取柜台内部委托编号
                long oid = entry.getKey();
                int counterOid = IDConverter.seperateLongToInt(oid)[1];
                updateAndNotify(counterOid, entry.getValue(), oidOrderMap.get(oid));
            }
        });
    }

    private void updateAndNotify(int counterOid, List<MatchData> value, OrderCmd orderCmd) {
        if (CollectionUtils.isEmpty(value)) {
            return;
        }
        //成交数据的处理
        for (MatchData md : value) {
            OrderStatus status = md.status;
            if (status == OrderStatus.TRADE_ED || status == OrderStatus.PART_TRADE) {
                //更新成交
                tradeService.saveTrade(counterOid, md, orderCmd);
                //持仓资金更新
                if (orderCmd.direction == OrderDirection.BUY) {
                    //B 13 30股 成交：10 10股
                    //这种情况需要释放资金(低价购买了股票)
                    if (orderCmd.price > md.price) {
                        balanceService.addBalance(orderCmd.uid, (orderCmd.price - md.price) * md.volume);
                    }
                    posiService.addPosi(orderCmd.uid, orderCmd.code, md.volume, md.price);
                } else if (orderCmd.direction == OrderDirection.SELL) {
                    balanceService.addBalance(orderCmd.uid, md.price * md.volume);
                } else {
                    log.error("wrong direction[{}]", orderCmd.direction);
                }
                //通知客户端
                socketVertx.eventBus().publish(TRADE_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                        JSONUtils.toJSONString(
                                ImmutableMap.of("code", orderCmd.code,
                                        "direction", orderCmd.direction,
                                        "volume", md.volume)
                        )
                );
            }
        }

        //委托变动
        //根据最后一笔Match处理委托
        MatchData finalMatchData = value.get(value.size() - 1);
        OrderStatus finalOrderStatus = finalMatchData.status;
        orderService.updateOrder(orderCmd.uid, counterOid, finalOrderStatus);
        if (finalOrderStatus == OrderStatus.CANCEL_ED || finalOrderStatus == OrderStatus.PART_CANCEL) {
            oidOrderMap.remove(orderCmd.oid);
            if (orderCmd.direction == OrderDirection.BUY) {   //撤买单
                //撤单请求最后的成交量为负
                balanceService.addBalance(orderCmd.uid, -(orderCmd.price * finalMatchData.volume));
            } else if (orderCmd.direction == OrderDirection.SELL) {  //撤卖单
                posiService.addPosi(orderCmd.uid, orderCmd.code, -finalMatchData.volume, orderCmd.price);
            } else {
                log.error("wrong direction[{}]", orderCmd.direction);
            }
        }
        //通知委托终端
        socketVertx.eventBus().publish(
                ORDER_NOTIFY_ADDR_PREFIX + orderCmd.uid,
                ""
        );
    }
}