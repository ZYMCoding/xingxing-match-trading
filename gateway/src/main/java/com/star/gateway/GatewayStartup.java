package com.star.gateway;

import com.star.gateway.config.GatewayConfig;
import lombok.extern.log4j.Log4j2;
import org.dom4j.DocumentException;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.impl.BodyCodecImpl;

import java.io.FileInputStream;
import java.io.InputStream;

@Log4j2
public class GatewayStartup {
    public static void main(String[] args) throws DocumentException {
        String configFileName = "gateway.xml";
        GatewayConfig config = new GatewayConfig();

        //将配置的xml文件作为输入流
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + "\\" + configFileName);
            log.info("gateway.xml exist in jar path");
        } catch (Exception e) {
            inputStream = GatewayStartup.class.getResourceAsStream("/" + configFileName);
            log.info("gateway.xml exist in jar file");
        }
        config.initConfig(inputStream);
        config.setBodyCodec(new BodyCodecImpl());
        config.setCheckSumImpl(new CheckSumImpl());
        config.startup();
    }
}
