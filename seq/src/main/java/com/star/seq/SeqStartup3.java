package com.star.seq;

import com.star.seq.bean.SeqConfig;
import thirdpart.codec.impl.BodyCodecImpl;

import java.io.IOException;

public class SeqStartup3 {

    public static void main(String[] args) throws IOException {
        String configName = "seq3.properties";
        SeqConfig seqConfig = new SeqConfig(configName, new BodyCodecImpl());
        seqConfig.startup();
    }
}
