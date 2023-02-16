package thirdpart.codec.api;

import io.vertx.core.buffer.Buffer;
import thirdpart.bean.CommonMsg;

public interface MsgCodec {

    Buffer encodeToBuffer(CommonMsg msg);

    CommonMsg decodeFromBuffer(Buffer buffer);
}
