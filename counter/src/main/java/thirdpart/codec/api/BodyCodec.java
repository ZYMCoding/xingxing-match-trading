package thirdpart.codec.api;

import com.alipay.remoting.exception.CodecException;

// TODO 修复：NoClassDefFoundError: com/caucho/hessian/io/SerializerFactory
public interface BodyCodec {

    /**
     * 将Java对象变成字节数组
     * 编码分为3种:
     * 1.jdk序列化: 性能不高，不适合网络传输
     * 2.转换为json字符串: 容易抓包，但适合浏览器
     * 3.自定义编码/解码: 对性能、安全性有要求
     */
    <T> byte[] serialize(T obj) throws CodecException;

    /**
     * 将字节数组转换为Java对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz) throws CodecException;
}
