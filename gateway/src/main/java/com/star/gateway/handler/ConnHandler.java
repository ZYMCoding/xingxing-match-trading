package com.star.gateway.handler;

import com.star.gateway.config.GatewayConfig;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;

@Log4j2
@Data
@RequiredArgsConstructor
public class ConnHandler implements Handler<NetSocket> {

    @NonNull
    private GatewayConfig gatewayConfig;

    //包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
    private static final int PACKET_HEADER_LENGTH = 4 + 1 + 2 + 2 + 2 + 1 + 8;
    @Override
    public void handle(NetSocket netSocket) {
        MsgHandler msgHandler = new MsgHandler(gatewayConfig.getBodyCodec());
        msgHandler.onConnect(netSocket);
        final RecordParser parser = RecordParser.newFixed(PACKET_HEADER_LENGTH);
        //设置报文接收处理器
        parser.setOutput(new Handler<Buffer>() {
            int bodyLength = -1;
            byte checksum = -1;
            short msgSrc = -1;
            short msgDst = -1;
            short msgType = -1;
            byte status = -1;
            long msgNo = -1;
            @Override
            public void handle(Buffer buffer) {
                if (bodyLength == -1) {
                    //读取报头
                    bodyLength = buffer.getInt(0);  //4字节
                    checksum = buffer.getByte(4);   //1字节
                    msgSrc = buffer.getShort(5);    //2字节
                    msgDst = buffer.getShort(7);
                    msgType = buffer.getShort(9);
                    status = buffer.getByte(11);
                    msgNo = buffer.getLong(12);     //8字节
                    parser.fixedSizeMode(bodyLength);
                } else {
                    //读取数据
                    byte[] bufferBytes = buffer.getBytes();
                    CommonMsg commonMsg;
                    //检验报体的校验和是否匹配
                    if (checksum != gatewayConfig.getCheckSumImpl().getCheckSum(bufferBytes)) {
                        log.error("illegal byte body exist from client:{}", netSocket.remoteAddress());
                        return;
                    } else {
                        if (msgDst != gatewayConfig.getId()) {
                            //报文的地址和配置的地址不一致（发错地方）
                            log.error("recv error msgDst dst:{} from client:{}", msgDst, netSocket.remoteAddress());
                            return;
                        }
                        //将数据封装为CommonMsg
                        commonMsg = new CommonMsg();
                        commonMsg.setBodyLength(bodyLength);
                        commonMsg.setChecksum(checksum);
                        commonMsg.setMsgSrc(msgSrc);
                        commonMsg.setMsgDst(msgDst);
                        commonMsg.setMsgType(msgType);
                        commonMsg.setStatus(status);
                        commonMsg.setMsgNo(msgNo);
                        commonMsg.setBody(bufferBytes);
                        commonMsg.setTimestamp(System.currentTimeMillis());

                        msgHandler.onCounterData(commonMsg);

                        //复原，以读取下一个报文
                        bodyLength = -1;
                        checksum = -1;
                        msgSrc = -1;
                        msgDst = -1;
                        msgType = -1;
                        status = -1;
                        msgNo = -1;
                        parser.fixedSizeMode(PACKET_HEADER_LENGTH);
                    }
                }
            }
        });
        netSocket.handler(parser);

        //异常 退出处理器
        netSocket.closeHandler(close -> {
            msgHandler.onDisConnect(netSocket);
        });
        netSocket.exceptionHandler(e -> {
            msgHandler.onException(netSocket, e);
            netSocket.close();
        });
    }
}
