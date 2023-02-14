package thirdpart.codec.impl;

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.SerializerManager;
import thirdpart.codec.api.BodyCodec;

public class BodyCodecImpl implements BodyCodec {

    @Override
    public <T> byte[] serialize(T obj) throws CodecException {
        byte[] bytes = SerializerManager.getSerializer(SerializerManager.Hessian2).serialize(obj);
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws CodecException {
        return SerializerManager.getSerializer(SerializerManager.Hessian2).deserialize(bytes, clazz.getName());
    }

}
