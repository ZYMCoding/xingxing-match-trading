package com.star.engine.core;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.star.engine.bean.RbCmdFactory;
import com.star.engine.bean.command.RbCmd;
import com.star.engine.handler.BaseHandler;
import com.star.engine.handler.exception.DisruptorExceptionHandler;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;
import thirdpart.order.CmdType;
import thirdpart.order.OrderCmd;

import java.util.Timer;
import java.util.TimerTask;

import static com.star.engine.handler.pub.L1PubHandler.HQ_PUB_RATE;

@Log4j2
@Data
public class EngineCore {

    private final Disruptor<RbCmd> disruptor;

    private static final int RING_BUFFER_SIZE = 1024;

    @NonNull
    private final EngineApi engineApi;

    public EngineCore(@NonNull final BaseHandler riskHandler,
                      @NonNull final BaseHandler matchHandler,
                      @NonNull final BaseHandler pubHandler
    ) {
        this.disruptor = new Disruptor<RbCmd>(
                new RbCmdFactory(),     //产生实例的工厂方法
                RING_BUFFER_SIZE,       //size：2的n次方
                new AffinityThreadFactory("aft_engine_core", AffinityStrategies.ANY),  //线程池(线程产生策略理论上在SAME_CORE时效率最高)
                ProducerType.SINGLE,        //只有一个生产者线程
                new BlockingWaitStrategy()
        );
        this.engineApi = new EngineApi(disruptor.getRingBuffer());

        //全局异常处理器
        final DisruptorExceptionHandler<RbCmd> exceptionHandler = new DisruptorExceptionHandler<>(
                "main",    //撮合核心的名字
                (ex, seq) -> {   //出现异常后的回调函数
                    log.error("exception thrown on seq = {}", seq, ex);
                }
        );
        disruptor.setDefaultExceptionHandler(exceptionHandler);

        //处理器处理顺序前置风控-->撮合-->发布数据
        disruptor.handleEventsWith(riskHandler)
                .then(matchHandler)
                .then(pubHandler);

        //启动
        disruptor.start();
        log.info("match engine start");

        //定时任务：发布行情
        new Timer().schedule(new HqPubTask(), 1000, HQ_PUB_RATE);
    }

    private class HqPubTask extends TimerTask {
        @Override
        public void run() {
            engineApi.submitCommand(OrderCmd.builder()
                    .type(CmdType.HQ_PUB)
                    .build());
        }
    }
}