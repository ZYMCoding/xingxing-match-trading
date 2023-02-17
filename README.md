# Element-ui + Spring Cloud + Vertx 的撮合交易系统
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
## 撮合核心
### Disruptor进行快速撮合

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