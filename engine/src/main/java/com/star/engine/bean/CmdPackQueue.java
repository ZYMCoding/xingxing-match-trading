package com.star.engine.bean;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.google.common.collect.Lists;
import com.star.engine.core.EngineApi;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import thirdpart.bean.CmdPack;
import thirdpart.codec.api.BodyCodec;
import thirdpart.order.OrderCmd;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * 缓存队列（单例模式）
 */
@Log4j2
@Data
public class CmdPackQueue {

    private static CmdPackQueue ourInstance = new CmdPackQueue();

    private CmdPackQueue() {
    }

    public static CmdPackQueue getInstance() {
        return ourInstance;
    }

    private final BlockingQueue<CmdPack> recvCache = new LinkedBlockingDeque<>();

    private RheaKVStore orderKVStore;

    private BodyCodec bodyCodec;

    private EngineApi engineApi;

    public void cache(CmdPack cmdPack) {
        recvCache.offer(cmdPack);
    }

    public void init(RheaKVStore orderKVStore, BodyCodec bodyCodec, EngineApi engineApi) {
        this.orderKVStore = orderKVStore;
        this.bodyCodec = bodyCodec;
        this.engineApi = engineApi;
        new Thread(() -> {
            while (true) {
                try {
                    //10s无法找到数据就返回null
                    CmdPack cmdPack = recvCache.poll(10, TimeUnit.SECONDS);
                    if (cmdPack != null) {
                        log.info(cmdPack);
                        handle(cmdPack);
                    }
                } catch (Exception e) {
                    log.error("msg pack recvcache error, continue", e);
                }
            }
        }).start();
    }

    private long lastPackNo = -1;

    /**
     * 完成对排队机数据的处理（包含广播中正常收取和出现异常并从排队机中抓取）
     * @param cmdPack 从缓存里获得的包对象
     * @throws CodecException 解码错误
     */
    private void handle(CmdPack cmdPack) throws CodecException {
//        log.info("recv: {}", cmdPack);
        //NACK
        long packNo = cmdPack.getPackNo();
        //上游约定包序号从0开始递增，可通过当前包序号判断是否丢包
        if (packNo == lastPackNo + 1) {    //正常情况
            if (CollectionUtils.isEmpty(cmdPack.getOrderCmds())) {
                return;
            }
            for (OrderCmd orderCmd : cmdPack.getOrderCmds()) {
                engineApi.submitCommand(orderCmd);
            }
            lastPackNo++;
        } else if (packNo <= lastPackNo) {
            //收到历史上重复的包:直接丢弃
            log.warn("recv duplicatge packId: {}", packNo);
        } else {
            //跳号，漏收包
            log.info("packNo lost from {} to {}, begin query from sequencer", lastPackNo + 1, packNo);
            //主动从排队机抓取数据
            byte[] firstKey = new byte[8];
            Bits.putLong(firstKey, 0, lastPackNo + 1);   //理论上缺失的第一个包(firstKey included)
            byte[] lastKey = new byte[8];
            Bits.putLong(lastKey, 0, packNo + 1);     //理论上缺失的最后一个包(lastKey excluded)
            final List<KVEntry> kvEntries = orderKVStore.bScan(firstKey, lastKey);
            if (CollectionUtils.isNotEmpty(kvEntries)) {
                //排队机中存在跳过的包
                List<CmdPack> collect = Lists.newArrayList();
                for (KVEntry entry : kvEntries) {
                    byte[] value = entry.getValue();
                    if (ArrayUtils.isNotEmpty(value)) {
                        collect.add(bodyCodec.deserialize(value, CmdPack.class));
                    }
                }
                //按照包的序号排序
                collect.sort((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo()));
                for (CmdPack pack : collect) {
                    //包中具有空体
                    if (CollectionUtils.isEmpty(pack.getOrderCmds())) {
                        continue;
                    }
                    for (OrderCmd orderCmd : pack.getOrderCmds()){
                        engineApi.submitCommand(orderCmd);
                    }
                }
            }
            //若排队机中缺失序列也为空，说明排队机本身丢失数据(出现异常)
            //直接更新lastPackNo(放弃中间跳过的数据)
            lastPackNo = packNo;
        }
    }
}