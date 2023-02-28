package com.star.engine.handler;

import com.lmax.disruptor.EventHandler;
import com.star.engine.bean.command.RbCmd;

public abstract class BaseHandler implements EventHandler<RbCmd> {

}
