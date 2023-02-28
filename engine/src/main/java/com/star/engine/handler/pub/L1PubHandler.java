package com.star.engine.handler.pub;

import com.alipay.remoting.exception.CodecException;
import com.star.engine.bean.command.RbCmd;
import com.star.engine.bean.orderbook.MatchEvent;
import com.star.engine.config.EngineConfig;
import com.star.engine.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import scala.util.matching.Regex;
import thirdpart.bean.CommonMsg;
import thirdpart.hq.L1MarketData;
import thirdpart.hq.MatchData;
import thirdpart.order.CmdType;

import java.util.List;

import static thirdpart.bean.MsgConstants.*;

@Log4j2
@RequiredArgsConstructor
public class L1PubHandler extends BaseHandler {

    //行情发送的频率，每隔5s发送一次数据
    public static final int HQ_PUB_RATE = 5000;

    /**
     * Map的key为柜台的id号，value为成交数据
     */
    @NonNull
    private final ShortObjectHashMap<List<MatchData>> matchEventMap;

    @NonNull
    private EngineConfig engineConfig;

    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        final CmdType cmdType = cmd.command;
        if (cmdType == CmdType.NEW_ORDER || cmdType == CmdType.CANCEL_ORDER) {
            for (MatchEvent event : cmd.matchEventList) {
                //新订单或者撤单时会将MatchData放到matchEventMap缓存中(每一个柜台号对应一个MatchData)
                matchEventMap.get(event.mid).add(event.copy());
            }
        } else if (cmdType == CmdType.HQ_PUB) {
            //返回5档行情
            pubMarketData(cmd.marketDataMap);

            //给柜台发送MatchData(撮合信息)
            pubMatchData();
        }
    }

    private void pubMatchData() {
        if (matchEventMap.size() == 0) {
            return;
        }
        log.info(matchEventMap);
        try {
            for (ShortObjectPair<List<MatchData>> s : matchEventMap.keyValuesView()){
                if (CollectionUtils.isEmpty(s.getTwo())) {
                    continue;
                }
                byte[] serialize = engineConfig.getBodyCodec().serialize(s.getTwo().toArray(new MatchData[0]));
                pubData(serialize, s.getOne(), MATCH_ORDER_DATA);

                //清空数据列表，避免每次重复发送数据
                s.getTwo().clear();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static final short HQ_ADDRESS = -1;

    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        log.info(marketDataMap);
        byte[] serialize = null;
        try {
            serialize = engineConfig.getBodyCodec().serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        } catch (Exception e) {
            log.error(e);
        }
        if (serialize == null) {
            return;
        }
        pubData(serialize, HQ_ADDRESS, MATCH_HQ_DATA);
    }

    private void pubData(byte[] serialize, short dst, short msgType) {
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(serialize.length);
        msg.setChecksum(engineConfig.getCheckSum().getCheckSum(serialize));
        msg.setMsgSrc(engineConfig.getId());
        msg.setMsgDst(dst);
        msg.setMsgType(msgType);
        msg.setStatus(NORMAL);
        msg.setBody(serialize);
        engineConfig.getBusSender().publish(msg);
    }

}
