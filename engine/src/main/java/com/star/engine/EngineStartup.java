package com.star.engine;

import com.star.engine.config.EngineConfig;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.impl.BodyCodecImpl;
import thirdpart.codec.impl.MsgCodecImpl;

import java.io.IOException;

public class EngineStartup {

    public static void main(String[] args) throws IOException {
        EngineConfig engineConfig = new EngineConfig(
                "engine.properties",
                new BodyCodecImpl(),
                new CheckSumImpl(),
                new MsgCodecImpl());
        engineConfig.startUp();
    }
}
