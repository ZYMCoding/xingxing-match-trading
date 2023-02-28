package com.star.engine.bean;

import com.google.common.collect.Lists;
import com.lmax.disruptor.EventFactory;
import com.star.engine.bean.command.CmdResultCode;
import com.star.engine.bean.command.RbCmd;
import javafx.event.EventHandler;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

public class RbCmdFactory implements EventFactory<RbCmd> {
    @Override
    public RbCmd newInstance() {
        return RbCmd.builder()
                .resultCode(CmdResultCode.SUCCESS)
                .matchEventList(Lists.newArrayList())
                .marketDataMap(new IntObjectHashMap<>())
                .build();
    }
}