import com.alipay.remoting.exception.CodecException;
import thirdpart.bean.CommonMsg;
import thirdpart.codec.api.BodyCodec;
import thirdpart.codec.impl.BodyCodecImpl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class CodecTest {

    public static void main(String[] args) throws UnsupportedEncodingException, CodecException {
        CommonMsg commonMsg = new CommonMsg();
        commonMsg.setMsgDst((short) 1);
        commonMsg.setErrCode((short) 12);
        BodyCodecImpl bodyCodec = new BodyCodecImpl();
        byte[] serialize = bodyCodec.serialize(commonMsg);
        System.out.println(bodyCodec.deserialize(serialize, CommonMsg.class));
    }
}
