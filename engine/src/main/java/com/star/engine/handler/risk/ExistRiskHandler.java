package com.star.engine.handler.risk;

import com.star.engine.bean.command.CmdResultCode;
import com.star.engine.bean.command.RbCmd;
import com.star.engine.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;
import thirdpart.order.CmdType;

@RequiredArgsConstructor
@Log4j2
public class ExistRiskHandler extends BaseHandler {

    @NonNull
    private MutableLongSet uidSet;

    @NonNull
    private MutableIntSet codeSet;

    /**
     * 发布行情Event,新委托Event,撤单Event,权限控制等
     * @param cmd        RbCmd类:published to the {@link com.lmax.disruptor.RingBuffer}
     * @param sequence   of the event being processed
     * @param endOfBatch flag to indicate if this is the last event in a batch from the {@link com.lmax.disruptor.RingBuffer}
     */
    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endOfBatch) throws Exception {
        //指令为系统产生的行情发布指令，则不用经过前置风控
        if (cmd.command == CmdType.HQ_PUB) {
            return;
        }
        if (cmd.command == CmdType.NEW_ORDER || cmd.command == CmdType.CANCEL_ORDER) {
            //非法用户ID
            if (!uidSet.contains(cmd.uid)) {
                log.info("illegal uid: {}", cmd.uid);
                cmd.resultCode = CmdResultCode.RISK_INVALID_CODE;
                return;
            }
            //非法股票代码
            if (!codeSet.contains(cmd.code)) {
                log.error("illegal code [{}] exist", cmd.code);
                cmd.resultCode = CmdResultCode.RISK_INVALID_CODE;
                return;
            }
        }
    }
}
