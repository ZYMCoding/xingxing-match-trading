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

        //时间优先(先来的站前面) 价格优先(买单价高先，卖单价低先) 量大优先
        orderCmds.sort(((o1, o2) -> {
            int res = compareTime(o1, o2);
            if (res != 0) {
                return res;
            }
            res = comparePrice(o1, o2);
            if (res != 0) {
                return res;
            }
            res = compareVolume(o1, o2);
            return res;
        }));

        // TODO 存储到KVStore 发送到撮合核心
    }

    private int compareTime(OrderCmd o1, OrderCmd o2) {
        if (o1.timestamp > o2.timestamp) {
            return 1;
        } else if (o1.timestamp < o2.timestamp) {
            return -1;
        } else {
            return 0;
        }
    }

    private int comparePrice(OrderCmd o1, OrderCmd o2) {
        if (o1.direction == o2.direction) {
            if (o1.price > o2.price) {
                //委托均为买时，价格高的在前，委托为卖，价格低的在前
                return o1.direction == OrderDirection.BUY ? -1 : 1;
            } else if (o1.price < o2.price) {
                return o1.direction == OrderDirection.BUY ? -1 : 1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    private int compareVolume(OrderCmd o1, OrderCmd o2) {
        if (o1.volume > o2.volume) {  //价格高在前
            return -1;
        } else if (o1.volume < o2.volume) {
            return 1;
        }
        return 0;
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
