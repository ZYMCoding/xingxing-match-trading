package com.star.gateway.config;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.star.gateway.container.OrderCmdContainer;
import com.star.gateway.handler.ConnHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.api.BodyCodec;
import thirdpart.fetchserv.api.FetchService;

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
        this.fetchServPort = Integer.parseInt(root.element("fetchservport").getText());
        log.info("GateWay ID:{}, Port:{}, FetchServPort: {}", id, recvPort, fetchServPort);
    }

    public void startup() {
        //启动TCP监听
        initRecv();

        //排队机交互
        ServerConfig serverConfig = new ServerConfig()
                .setPort(fetchServPort)
                .setProtocol("bolt");
        ProviderConfig<FetchService> providerConfig = new ProviderConfig<FetchService>()
                .setInterfaceId(FetchService.class.getName())
                //指定FetchService实现类来实现响应(直接从单例的Container获取即可)，理论上应该传入FetchService的实现类
                .setRef(() -> OrderCmdContainer.getInstance().getAll())
                .setServer(serverConfig);
        providerConfig.export();
        log.info("gateway startup fetchServ success at port: {}", fetchServPort);
    }

    public void initRecv() {
        NetServer netServer = vertx.createNetServer();
        netServer.connectHandler(new ConnHandler(this));
        netServer.listen(recvPort, res -> {
            if (res.succeeded()) {
                log.info("gateway startup at port: {}", recvPort);
            } else {
                log.error("gateway startup fail");
            }
        });
    }
}
