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
    TcpDirectSender tcpDirectSender;

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
        log.info("TCPDirectSender start");
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
        msg.setBody(data);
        return msg;
    }

    public void sendOrder(OrderCmd orderCmd) {
        //将OrderCmd打包为CommonMsg
        byte[] data;
        try {
            data = bodyCodec.serialize(orderCmd);   //将对象转为字节码放到报体中(Hessian2编码方式)
        } catch (Exception e) {
            log.error("encode error for ordercmd:{}", orderCmd, e);
            return;
        }
        CommonMsg commonMsg = generateMsg(data, counterProperty.getId(), counterProperty.getGatewayId(), COUNTER_NEW_ORDER, NORMAL, snowflakeIdWorker.nextId());
        //将封装好的CommonMsg编码为Buffer(按字节索引组装)
        boolean send = tcpDirectSender.send(msgCodec.encodeToBuffer(commonMsg));
        log.info("ordercmd has bean sent? {}", send);
    }
}
