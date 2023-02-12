package com.star.counter.gateway;

import com.star.counter.property.CounterProperty;
import com.star.counter.util.SnowflakeIdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import thirdpart.bean.CommonMsg;
import thirdpart.checksum.api.CheckSum;
import thirdpart.codec.api.BodyCodec;
import thirdpart.codec.api.MsgCodec;
import thirdpart.order.OrderCmd;
import thirdpart.tcp.TcpDirectSender;

import javax.annotation.PostConstruct;

import static thirdpart.bean.MsgConstants.COUNTER_NEW_ORDER;
import static thirdpart.bean.MsgConstants.NORMAL;

@Log4j2
@Component
public class MsgTrans {

    @Autowired
    private TcpDirectSender tcpDirectSender;

    @Autowired
    BodyCodec bodyCodec;

    @Autowired
    private CheckSum checkSum;

    @Autowired
    private CounterProperty counterProperty;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    MsgCodec msgCodec;

    @PostConstruct
    private void init() {
        tcpDirectSender.startUp();
    }

    private CommonMsg generateMsg(byte[] data, short msgSrc, short msgDst, short msgType, byte status, long msgNo) {
        if (data == null) {
            log.error("empty body, not send");
            return null;
        }
        CommonMsg msg = new CommonMsg();
        msg.setBodyLength(data.length);
        msg.setChecksum(checkSum.getCheckSum(data));
        msg.setMsgSrc(msgSrc);
        msg.setMsgDst(msgDst);
        msg.setMsgType(msgType);
        msg.setStatus(status);
        msg.setMsgNo(msgNo);
        return msg;
    }

    public void sendOrder(OrderCmd orderCmd) {
        //将OrderCmd打包为CommonMsg
        byte[] data = null;
        try {
            bodyCodec.serialize(orderCmd);
        } catch (Exception e) {
            log.error("encode error for ordercmd:{}", orderCmd, e);
            return;
        }
        CommonMsg commonMsg = generateMsg(data, counterProperty.getId(), counterProperty.getGatewayId(), COUNTER_NEW_ORDER, NORMAL, snowflakeIdWorker.nextId());
        tcpDirectSender.send(msgCodec.encodeToBuffer(commonMsg));
    }
}
