package com.star.seq.bean;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.*;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.listener.ChannelListener;
import com.alipay.sofa.rpc.transport.AbstractChannel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import thirdpart.codec.api.BodyCodec;
import thirdpart.fetchserv.api.FetchService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

/**
 * 对KVStore节点的配置类
 * 读取properties里的属性作为配置作为Node的配置
 */
@Log4j2
@Data
@ToString
@RequiredArgsConstructor
public class SeqConfig {

    /**
     * 存放数据/日志的本地路径
     */
    private String dataPath;

    /**
     * 当前节点服务的地址
     */
    private String serveUrl;

    /**
     * KVStore集群有哪些地址
     */
    private String serverList;

    @NonNull
    private String fileName;

    private Node node;

    private String fetchUrls;

    @ToString.Exclude
    private Map<String, FetchService> fetchServiceMap = Maps.newConcurrentMap();

    @NonNull
    @ToString.Exclude
    private BodyCodec bodyCodec;

    @RequiredArgsConstructor
    private class FetchChannelListener implements ChannelListener {

        @NonNull
        private ConsumerConfig<FetchService> config;


        @Override
        public void onConnected(AbstractChannel abstractChannel) {
            String remoteAddr = abstractChannel.remoteAddress().toString();
            log.info("connect to gateway: {}", remoteAddr);
            fetchServiceMap.put(remoteAddr, config.refer());   //key为连接地址，value为代理的服务类
        }

        @Override
        public void onDisconnected(AbstractChannel abstractChannel) {
            String remoteAddr = abstractChannel.remoteAddress().toString();
            log.info("disconnect to gateway: {}", remoteAddr);
            fetchServiceMap.remove(remoteAddr);
        }
    }

    /**
     * 从配置文件中读取属性并赋值给此对象
     * 初始化KVStore并监听监听节点状态
     * @throws IOException 读取properties文件流时的异常
     */
    public void startup() throws IOException {
        //读取配置文件
        initConfig();

        //初始化集群
        starSeqDbCluster();

        // TODO 启动下游广播

        // TODO 初始化网关连接
        startupFetch();
    }

    /**
     * 从网关中获得数据
     * 逻辑:
     * 1.从哪些网关抓取
     * 2.通信方式
     */
    private void startupFetch() {
        //将所有的网关连接放到Map中
        String[] urls = fetchUrls.split(";");
        for (String url : urls) {
            //排队机为RPC框架中的消费者
            ConsumerConfig<FetchService> consumerConfig = new ConsumerConfig<FetchService>()
                    .setInterfaceId(FetchService.class.getName())   //连接的接口，上下游通信标准
                    .setProtocol("bolt")    //RPC通信的协议:bolt
                    .setTimeout(5000)       //超时设置
                    .setDirectUrl(url);     //直连地址
            consumerConfig.setOnConnect(Lists.newArrayList(new FetchChannelListener(consumerConfig)));  //可增加多个连接监听器,但列表只放一个
            fetchServiceMap.put(url, consumerConfig.refer());    //Consumer第一次连上Provider时Listener中的onConnected不会执行
        }

        /*
          使用jdk的Timer进行从网关定时抓取数据的任务
          参数分别为要执行的线程，延迟执行的时间，执行的频率
         */
        new Timer().schedule(new FetchTask(this), 5000, 1000);
    }

    /**
     * 启动KVStore
     */
    private void starSeqDbCluster() {
        String[] split = serveUrl.split(":");
        String ip = split[0];
        int port = Integer.parseInt(split[1]);
        //对KV数据库存储引擎进行相关设置
        final StoreEngineOptions storeEngineOptions = StoreEngineOptionsConfigured.newConfigured()
                .withStorageType(StorageType.Memory)    //使用memory来存储Raft的数据
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config())
                .withRaftDataPath(dataPath)
                .withServerAddress(new Endpoint(ip, port))
                .config();
        //针对集群中多个Raft的配置，实际中只有一个集群，所以指定了Fake
        final PlacementDriverOptions placementDriverOptions = PlacementDriverOptionsConfigured.newConfigured()
                .withFake(true)
                .config();
        //将以上配置配置到KV储存选项中
        final RheaKVStoreOptions rheaKVStoreOptions = RheaKVStoreOptionsConfigured.newConfigured()
                .withInitialServerList(serverList)
                .withStoreEngineOptions(storeEngineOptions)
                .withPlacementDriverOptions(placementDriverOptions)
                .config();
        node = new Node(rheaKVStoreOptions);
        node.start();
        //将节点的stop方法挂载在jdk的shutdown流程中
        Runtime.getRuntime().addShutdownHook(new Thread(node :: stop));
    }

    private void initConfig() throws IOException {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        this.dataPath = properties.getProperty("datapath");
        this.serveUrl = properties.getProperty("serveurl");
        this.serverList = properties.getProperty("serverlist");
        this.fetchUrls = properties.getProperty("fetchurls");
        log.info("datapath is: {}", this.dataPath);
        log.info("serverurl is: {}", this.serveUrl);
        log.info("serverlist is: {}", this.serverList);
    }

}
