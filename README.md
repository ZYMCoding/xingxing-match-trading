# Element-ui + Spring Boot + Vertx 的撮合交易系统
## 体验地址：http://47.113.188.108/
账户1：uid：10000546 password：zxc123

账户2：uid：10000555 password：qwe321

## 前端代码在cfront下，使用Element-UI
## 柜台服务：
### 相关技术：
#### 1. 登录
用户发出登录请求时，通过`com.star.counter.util.SnowflakeIdWorker`产生唯一的UUID，

JDK自带的UUID在系统吞吐过大时无法满足UUID的唯一性，[TwitterSnowflakeUUID](https://github.com/beyondfengyu/SnowFlake)
#### 2. 账户持仓、资金等信息的查询
#### 3. 发送买/卖委托
用户进行买/卖操作时，修改数据库中用户相关信息并将相关数据发送到网关

发送前需要将订单信息的类通过`Hessian2`进行序列化作为发送报文的包体

之后将报体长度、信息类型、校验和等信息作为报头和报体一起组装成为CommonMsg类并发送

发送时先将封装好的指令放入到阻塞队列中，创建线程对阻塞队列进行轮询，轮询到结果后写入vertx的NetSocket
## 网关
### 功能
1. 启动vertx的server端，通过跟柜台client端相同的方法解开TCP包并同样通过`Hessian2`转换成委托类对象
2. 使用sofa-rpc，将委托类对象写入单例的Container的阻塞队列中，然后创建`public List<OrderCmd> getAll()`方法作为Provider的实现类
3. 发布服务以便排队机抓取
## 排队机
### 设计目的
为了保证交易的公平性，排除网络延迟等外部因素对委托排序的影响，

所以需要排队机来主动从各个网关获取订单信息并进行排序，排序规则如下：
1. 时间优先
2. 价格优先
3. 量优先
### 设计要求
1. 可用性：排队机中有节点DOWN掉后，排队机集群仍应保持服务
2. 一致性：排队机集群中的数据应该保持一致性
3. 根据CAP三角理论，应该在可用性和一致性之间做出取舍
### 强一致性算法Raft
1. [Leader election](https://youjiali1995.github.io/raft/etcd-raft-leader-election/)：使用主节点来引导所有数据同步
2. Log Replication：日志复制，将日志同步到所有节点上
3. 各个节点通过选举产生主节点，主节点将数据同步到其他节点上，主节点DOWN掉后，其他节点尝试重连并选举产生新的主节点，新节点的数据应该保持最新
### 重要流程
1. `new Timer().schedule(new FetchTask(this), 5000, 1000);`，通过jdk的Timer每1000ms进行抓取数据
2. UDP广播，使用UDP的原因如下：
    - UDP相较于TCP传输的报文简单，性能更好
    - 排队机到撮合核心之间一般为内网环境，网络稳定，丢包概率小
    - 在撮合核心接收时对自动递增的包号进行校验，从而判断从广播中获得的数据是否可用，解决UDP丢包问题
    - 出现丢包状况时，撮合核心可以主动从KVStore里获取
    - UDP支持一对多(组播)，可应对多个撮合核心
```Java
    //发送到撮合核心
    seqConfig.getMulticastSender().send(
            Buffer.buffer(serialize),    //vertx包装好的Buffer类
            seqConfig.getMulticastPort(),
            seqConfig.getMulticastIp(),
            null    //不配置异步处理器
    );
```
## 撮合核心
### 流程分析
#### 接收委托流
1. 从排队机的广播中获取CmdPack字节流，字节流进行反序列化后根据packNo判断是否丢包
2. 若广播中的序号更小，则说明丢包
3. 出现丢包时，主动从排队机KVStore里抓取
#### 数据结构
1. 每一只股票对应一个OrderBook, OrderBook里的每一个价格对应一个OrderBucket
2. OrderBook为NavigableMap(key具有一定顺序)，OrderBucket为LinkedHashMap(内部维护一个链表，具有顺序)
3. 关键代码

OrderBook里的sellBuckets和buyBuckets(buyBuckets的价格为倒序排列)
```Java
   // 将OrderBucket放入到Map中:key为价格
   private final NavigableMap<Long, OrderBucket> sellBuckets = new TreeMap<>();
   private final NavigableMap<Long, OrderBucket> buyBuckets = new TreeMap<>(Collections.reverseOrder());
```

OrderBucket里的LinkedHashMap(存放委托), 存放的订单从排队机获取
```Java
    //使用LinkedHashMap保存委托信息(key:委托序号, value:委托类)
    //LinkedHashMap内维护一个链表，可以按照顺序进行排列
    private final LinkedHashMap<Long, Order> entries = new LinkedHashMap<>();
```
#### 发布撮合结果
通过MQTT协议进行在总线上进行发布,Vertx-mqtt提供了Mqtt-Client
### Disruptor进行快速撮合
Disruptor的使用方式主要是加Handler，注意Handler的顺序，前置风控 -> 撮合 -> 发布处理

[Disruptor核心概念-中文](https://juejin.cn/post/6844903958180265997#heading-11)

[LMAX Disruptor User Guide](https://lmax-exchange.github.io/disruptor/user-guide/index.html)

#### 定长数组/预加载
数组空间预先分配（Event对象事先准备），线程只会对Event进行赋值操作 
#### CAS代替锁
单线程写避免锁的使用
#### Cache Padding（避免False share）：
[理解Disruptor（上）-带你体会CPU高速缓存的风驰电掣](https://blog.csdn.net/weixin_30235225/article/details/102054127)
- 缓存以缓存行作为基本单位，一个缓存行可储存不同数
- 不同数据被不同线程修改后会导致缓存行失效（CPU Cache的MESI协议）
- Cache Padding使每个缓存行只缓存一个数据防止对缓存的修改，虽然降低缓存利用率，但是提高缓存命中率
#### Memory Barrier
[中文文档-揭秘内存屏障](https://developer.aliyun.com/article/88523)

- 使用volatile字段进行写操作，每次写操作后内存会将就的数据flush掉从而保证所有线程拿到的是最新的值
- 下游消费者对上游消费者进行追踪，下游消费者拿到上游更新过的序列号之后才可以进行修改
- 总之，Disruptor高性能的原理就在于实现无锁结构
### 撮合关键代码
满足撮合的订单：买盘中大于等于报卖价的委托，卖盘中小于等于报买价的委托
```Java
    private long preMatch(RbCmd cmd, SortedMap<Long, OrderBucket> matchingBuckets) {
        int tVol = 0;
        if (matchingBuckets.size() == 0) {  //没有符合撮合的bucket
            return tVol;
        }
        List<Long> emptyBuckets = Lists.newArrayList();
        for (OrderBucket bucket : matchingBuckets.values()) {
            tVol += bucket.match(cmd.volume - tVol, cmd, order -> oidMap.remove(order.getOid()));
            if (bucket.getTotalVolume() == 0) {   //bucket中的委托耗尽
                emptyBuckets.add(bucket.getPrice());
            }
            if (tVol == cmd.volume) {   //新来的订单所有量被吃掉
                break;
            }
        }
        emptyBuckets.forEach(matchingBuckets :: remove);
        return tVol;
    }
```
