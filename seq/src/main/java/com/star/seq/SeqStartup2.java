package com.star.seq;

import com.star.seq.bean.SeqConfig;
import thirdpart.codec.impl.BodyCodecImpl;

import java.io.IOException;

public class SeqStartup2 {

    public static void main(String[] args) throws IOException {
        String configName = "seq2.properties";
        SeqConfig seqConfig = new SeqConfig(configName, new BodyCodecImpl());
        seqConfig.startup();
    }
}
