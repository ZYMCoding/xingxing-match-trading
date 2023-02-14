package com.star.gateway.handler;

import com.star.gateway.container.OrderCmdContainer;
import io.vertx.core.net.NetSocket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import thirdpart.bean.CommonMsg;
import thirdpart.codec.api.BodyCodec;
import thirdpart.order.OrderCmd;

@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class MsgHandler {

    private BodyCodec bodyCodec;

    void onConnect(NetSocket socket) {

    }

    void onDisConnect(NetSocket socket) {

    }

    void onException(NetSocket socket, Throwable e) {

    }

    void onCounterData(CommonMsg msg) {
        OrderCmd orderCmd;
        try {
            //将包体内容转为OrderCmd对象
            orderCmd = bodyCodec.deserialize(msg.getBody(), OrderCmd.class);
            //正式开发环境中不能在运行时输出
            log.info("recv cmd:{}", orderCmd);
            log.info(orderCmd.toString());
            //写入内存：将OrderCmd对象存储到静态OrderCmdContainer内，失败时日志输出
            if (!OrderCmdContainer.getInstance().cache(orderCmd)) {
                log.error("gateway queue insert fail, queue length:{}, order:{}", OrderCmdContainer.getInstance().size(), orderCmd);
            }
        } catch (Exception e) {
            log.error("decode order cmd error", e);
        }
    }
}
