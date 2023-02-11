package com.star.gateway.config;

import com.star.gateway.handler.ConnHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import thirdpart.checksum.api.CheckSum;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.api.BodyCodec;

import java.io.File;

@Log4j2
@Data
public class GatewayConfig {

    //网关ID
    private short id;

    //外网端口
    private int recvPort;

    //排队机抓取服务端口
    private int fetchServPort;

    private BodyCodec bodyCodec;

    private CheckSumImpl checkSumImpl;

    private Vertx vertx = Vertx.vertx();

    public void initConfig(String fileName) throws DocumentException {
        //dom4j对xml进行解析
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(fileName));
        Element root = document.getRootElement();
        this.id = Short.parseShort(root.element("id").getText());
        this.recvPort = Integer.parseInt(root.element("recvport").getText());
        log.info("GateWay ID:{}, Port:{}", id, recvPort);
    }

    public void startup() {
        //启动TCP监听

        //TODO 排队机交互
    }

    public void initRecv() {
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new ConnHandler(this));
    }
}
