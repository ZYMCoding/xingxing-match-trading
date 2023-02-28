import com.star.engine.config.EngineConfig;
import lombok.extern.slf4j.Slf4j;
import thirdpart.checksum.impl.CheckSumImpl;
import thirdpart.codec.impl.BodyCodecImpl;
import thirdpart.codec.impl.MsgCodecImpl;

import java.io.IOException;

@Slf4j
public class testEngineConfig {
    public static void main(String[] args) throws IOException {
        EngineConfig engineConfig = new EngineConfig(
                "engine.properties",
                new BodyCodecImpl(),
                new CheckSumImpl(),
                new MsgCodecImpl());
        engineConfig.startUp();
    }
}
