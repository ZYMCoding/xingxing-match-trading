package com.star.seq.bean;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import thirdpart.fetchserv.api.FetchService;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

@Log4j2
@RequiredArgsConstructor
public class FetchTask extends TimerTask {

    @NonNull
    private SeqConfig seqConfig;

    @Override
    public void run() {
        //从Map中拿到所有链接(只有主节点进行抓取)
        if (!seqConfig.getNode().isLeader()) {
            return;
        }
        Map<String, FetchService> fetchServiceMap = seqConfig.getFetchServiceMap();
        if (MapUtils.isEmpty(fetchServiceMap)) {  //该Util方法考虑到空元素和null的情况
            return;
        }
        //从网关收取数据
        List<OrderCmd> orderCmds = collectAllOrders(fetchServiceMap);
        if (CollectionUtils.isEmpty(orderCmds)) {
            return;
        }
        log.info(orderCmds);

        //对数据进行定序
        //时间优先(先来的站前面) 价格优先 量优先
        orderCmds.sort(((o1, o2) -> {
            if (o1.timestamp > o2.timestamp) {
                return 1;   //o1排后面
            } else if (o1.timestamp < o2.timestamp) {
                return -1;
            } else { //比价格，需要根据交易方向排序
                if (o1.direction == OrderDirection.BUY) {
                    if (o1.direction == o2.direction) {  //当委托均为买委托
                        //买价格高排在前面
                        if (o1.price > o2.price) {
                            return -1;
                        } else if (o1.price < o2.price) {
                            return 1;
                        } else {
                            //量比较
                        }
                    } else {
                        //方向不同，不影响结果（不用排序）
                        return 0;
                    }
                } else if (o1.direction == OrderDirection.SELL) {
                    if (o1.direction == o2.direction) {  //当委托均为卖委托
                        //卖价格低排在前面
                        if (o1.price > o2.price) {
                            return 1;
                        } else if (o1.price < o2.price) {
                            return -1;
                        } else {
                            //量比较
                        }
                    } else {
                        //方向不同，不影响结果（不用排序）
                        return 0;
                    }
                } else {
                    return 1;
                }
            }
        }));

    }

    private List<OrderCmd> collectAllOrders(Map<String, FetchService> fetchServiceMap) {
        List<OrderCmd> msgs = Lists.newArrayList();
        fetchServiceMap.values().forEach(fetchService -> {
            List<OrderCmd> orderCmds = fetchService.fetchData();
            if (CollectionUtils.isNotEmpty(orderCmds)) {
                msgs.addAll(orderCmds);
            }
        });
        return msgs;
    }
}
