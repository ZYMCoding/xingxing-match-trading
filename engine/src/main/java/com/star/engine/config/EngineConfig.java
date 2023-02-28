package com.star.engine.config;

import com.alipay.remoting.exception.CodecException;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.google.common.collect.Lists;
import com.star.engine.bean.CmdPackQueue;
import com.star.engine.bean.DBUtil;
import com.star.engine.bean.orderbook.api.OrderBook;
import com.star.engine.bean.orderbook.impl.OrderBookImpl;
import com.star.engine.core.EngineApi;
import com.star.engine.handler.BaseHandler;
import com.star.engine.handler.match.StockMatchHandler;
import com.star.engine.handler.pub.L1PubHandler;
import com.star.engine.handler.risk.ExistRiskHandler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import thirdpart.bean.CmdPack;
import thirdpart.bus.api.BusSender;
import thirdpart.bus.impl.MQTTBusSenderImpl;
import thirdpart.checksum.api.CheckSum;
import thirdpart.codec.api.BodyCodec;
import thirdpart.codec.api.MsgCodec;
import thirdpart.hq.MatchData;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.*;

@Log4j2
@RequiredArgsConstructor
@ToString
@Data
public class EngineConfig {

    private short id;

    //排队机广播地址
    private String orderRecvIp;

    //排队机广播的端口号
    private int orderRecvPort;

    //排队机集群的链接
    private String seqUrlList;

    //行情发布的地址
    private String pubIp;

    //行情发布的端口号
    private int pubPort;

    //配置文件的文件名
    @NonNull
    private String fileName;

    @NonNull
    private BodyCodec bodyCodec;

    @NonNull
    private CheckSum checkSum;

    @NonNull
    private MsgCodec msgCodec;

    private EngineApi engineApi;

    private Vertx vertx = Vertx.vertx();

    @ToString.Exclude
    private final RheaKVStore rheaKVStore = new DefaultRheaKVStore();

    public void startUp() throws IOException {
        //读取配置文件
        initConfig();

        //启动撮合核心
        startEngine();

        //建立总线连接，初始化数据发送
        initPub();

        //初始化接受排队机的数据以及连接
        startSeqConn();

    }

    private BusSender busSender;

    private void initPub() {
        busSender = new MQTTBusSenderImpl(pubIp, pubPort, msgCodec, vertx);
        busSender.startUp();
    }

    private void startEngine() {

        DBUtil dbUtil = DBUtil.getInstance();

        //得到用户id的set
        List<Map<String, Object>> allBalance = dbUtil.queryAllBalance();
        Set<Long> uidSet = new HashSet<>();
        for (Map<String, Object> balanceInfo : allBalance){
            Long uid = (Long) balanceInfo.get("uid");
            uidSet.add(uid);
        }
        LongHashSet uidLongSet = new LongHashSet();
        for (Long uid : uidSet) {
            uidLongSet.add(uid);
        }

        //得到所有股票代码的set
        HashSet<Integer> stockCodeSet = dbUtil.queryAllStockCode();
        IntHashSet stockCodeIntSet = new IntHashSet();
        for (Integer stockCode : stockCodeSet) {
            stockCodeIntSet.add(stockCode);
        }

        //得到所有的会员id
        List<Integer> memberIds = dbUtil.queryAllMemberIds();

        //前置风控处理器
        final BaseHandler riskHandler = new ExistRiskHandler(uidLongSet, stockCodeIntSet);

        //撮合处理器(订单簿)，撮合的核心同时提供行情的查询
        //给每一个股票定义一个订单簿
        IntObjectHashMap<OrderBook> orderBookMap = new IntObjectHashMap<>();
        stockCodeIntSet.forEach(code -> orderBookMap.put(code, new OrderBookImpl(code)));
        final BaseHandler matchHandler = new StockMatchHandler(orderBookMap);

        //发布处理器
        ShortObjectHashMap<List<MatchData>> matchEventMap = new ShortObjectHashMap<>();
        for (int id : memberIds) {
            matchEventMap.put((short) id, Lists.newArrayList());
        }
        final BaseHandler pubHandler = new L1PubHandler(matchEventMap, this);

    }


    /**
     * 初始化接受排队机的数据以及连接
     */
    private void startSeqConn() {
        //初始化排队机的连接
        final List<RegionRouteTableOptions> regionRouteTableOptions = MultiRegionRouteTableOptionsConfigured.newConfigured()
                .withInitialServerList(-1L, seqUrlList)
                .config();

        final PlacementDriverOptions pdOptions = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .withRegionRouteTableOptionsList(regionRouteTableOptions)
                .config();

        final RheaKVStoreOptions options = RheaKVStoreOptionsConfigured.newConfigured()
                .withPlacementDriverOptions(pdOptions)
                .config();

        rheaKVStore.init(options);

        //接受来自排队机的数据
        //初始化Cmd的缓存类
        CmdPackQueue.getInstance().init(rheaKVStore, bodyCodec, engineApi);

        //接收数据(组播，允许多个核心接受相同的数据包)
        DatagramSocket datagramSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
        datagramSocket.listen(orderRecvPort, "0.0.0.0", asyncRes -> {
            if (asyncRes.succeeded()) {
                //处理接收到的数据
                datagramSocket.handler(packet -> {
                    Buffer udpData = packet.data();
                    if (udpData.length() > 0) {
                        try {
                            CmdPack cmdPack = bodyCodec.deserialize(udpData.getBytes(), CmdPack.class);
                            log.info("cmdPack is {}", cmdPack);
                            CmdPackQueue.getInstance().cache(cmdPack);
                        } catch (CodecException e) {
                            log.error("decode packet error", e);
                        }
                    } else {
                        //接收到空数据的包
                        log.error("recv empty udp packet from client: {}", packet.sender().toString());
                    }
                });
                try {
                    //组播
                    datagramSocket.listenMulticastGroup(
                            orderRecvIp,        //排队机地址
                            mainInterface().getName(),   //需要监听的网卡名字
                            null,           //监听的地址(不需要)
                            asyncRes2 -> {  //处理器进行判断，看是否加入组播
                                log.info("listen succeed {}", asyncRes2.succeeded());
                            }
                    );
                } catch (Exception e) {
                    log.error(e);
                }
            } else {
                //打印监听失败的错误日志
                log.error("Listen failed, ", asyncRes.cause());
            }
        });
    }

    /**
     * 适合接受广播的网卡条件：
     * 1.!loopback
     * 2.支持multicast
     * 3.非虚拟机的网卡
     * 4.有IPV4地址
     *
     * @return 找出适合接受UDP广播的网卡
     */
    private static NetworkInterface mainInterface() throws Exception {
        final ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        NetworkInterface networkInterface = interfaces.stream().filter(t -> {
            try {
                boolean isLoopback = t.isLoopback();
                boolean supportsMulticast = t.supportsMulticast();
                boolean isVirtualBox = t.getDisplayName().contains("VirtualBox") || t.getDisplayName().contains("Host-only");
                boolean hasIpv4 = t.getInterfaceAddresses().stream().anyMatch(ia -> ia.getAddress() instanceof Inet4Address);
                return !isLoopback && supportsMulticast && !isVirtualBox && hasIpv4;
            } catch (Exception e) {
                log.error("find net interface error", e);
            }
            return false;
        }).sorted(Comparator.comparing(NetworkInterface::getName)).findFirst().orElse(null);  //找到第一个符合条件的网卡，否则返回空
        return networkInterface;
    }

    /**
     * 从配置文件中加载属性到类中
     *
     * @throws IOException 读取文件流的错误
     */
    private void initConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream(fileName));
        this.id = Short.parseShort(properties.getProperty("id"));
        this.orderRecvIp = properties.getProperty("orderrecvip");
        this.orderRecvPort = Integer.parseInt(properties.getProperty("orderrecvport"));
        this.seqUrlList = properties.getProperty("sequrllist");
        this.pubIp = properties.getProperty("pubip");
        this.pubPort = Integer.parseInt(properties.getProperty("pubport"));
        log.info(this);
    }
}
