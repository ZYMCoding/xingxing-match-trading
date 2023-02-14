package com.star.gateway;

import com.star.gateway.config.GatewayConfig;
import lombok.extern.log4j.Log4j2;
import org.dom4j.DocumentException;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.impl.BodyCodecImpl;

@Log4j2
public class GatewayStartup {
    public static void main(String[] args) throws DocumentException {
        String configFileName = "gateway.xml";
        GatewayConfig config = new GatewayConfig();
        config.initConfig(GatewayStartup.class.getResource("/").getPath() + configFileName);
        config.setBodyCodec(new BodyCodecImpl());
        config.setCheckSumImpl(new CheckSumImpl());
        config.startup();
    }
}
