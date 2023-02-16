package com.star.seq.bean;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import thirdpart.fetchserv.api.FetchService;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.stream.Collectors;

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

        // TODO 对数据进行定序
    }

    private List<OrderCmd> collectAllOrders(Map<String, FetchService> fetchServiceMap) {
        List<OrderCmd> orderCmdList = fetchServiceMap.values().stream()
                .map(t -> t.fetchData())
                .filter(msg -> CollectionUtils.isEmpty(msg))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
