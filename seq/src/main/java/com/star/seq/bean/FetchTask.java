package com.star.seq.bean;

import com.alipay.sofa.jraft.util.Bits;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.google.common.collect.Lists;
import io.vertx.core.buffer.Buffer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import thirdpart.fetchserv.api.FetchService;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * 该类主要负责从网关中抓取数据
 * 继承TimerTask类，从而实现定时功能，任务在run方法中执行
 * 只有主节点才执行抓取任务
 */
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
        try {
            //生成PackNo并打包成Cmd
            long packNo = getPackNoFromStore();
            CmdPack cmdPack = new CmdPack(packNo, orderCmds);

            //入库,将类使用Hessian2转换为字节流并存到KVStore里
            byte[] serialize = seqConfig.getBodyCodec().serialize(cmdPack);
            insertToKVStore(packNo, serialize);

            //更新PackNo++
            updatePacketNoInStore(packNo + 1);

            //发送到撮合核心
            seqConfig.getMulticastSender().send(
                    Buffer.buffer(serialize),    //vertx包装好的Buffer类
                    seqConfig.getMulticastPort(),
                    seqConfig.getMulticastIp(),
                    null    //不配置异步处理器
            );
        } catch (Exception e) {
            log.error("encode cmd error ", e);
        }
    }

    //将key写为byte数组的形式
    private static final byte[] PACKET_NO_KEY = BytesUtil.writeUtf8("seq_pqcket_no");

    /**
     * 从KVStore里取PacketNo,如果空的话则返回0
     * @return 查询到的PacketNo
     */
    private long getPackNoFromStore() {
        //得到对应的packetNo的字节数组
        final byte[] bPacketNo = seqConfig.getNode().getRheaKVStore().bGet(PACKET_NO_KEY);
        long packetNo = 0;
        if (ArrayUtils.isNotEmpty(bPacketNo)) {
            packetNo = Bits.getLong(bPacketNo, 0);
        }
        return packetNo;
    }

    /**
     * 将CmdPack的字节数组保存到KVStore中
     * @param packNo 包序号
     * @param serialize 包类通过序列化得到的字节数组
     */
    private void insertToKVStore(long packNo, byte[] serialize) {
        byte[] key = new byte[8];
        Bits.putLong(key, 0, packNo);    //将long型的包名转换为8位长的字节数组
        seqConfig.getNode().getRheaKVStore().put(key, serialize);   //put是异步操作，提高性能
    }

    /**
     * 更新PacketNo
     * @param newPackNo 要更新的包序号
     */
    private void updatePacketNoInStore(long newPackNo) {
        final byte[] bytes = new byte[8];
        Bits.putLong(bytes, 0, newPackNo);
        seqConfig.getNode().getRheaKVStore().put(PACKET_NO_KEY, bytes);
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
