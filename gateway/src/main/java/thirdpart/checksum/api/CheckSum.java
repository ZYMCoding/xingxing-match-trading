package thirdpart.checksum.api;

public interface CheckSum {

    /**
     * 得到校验和
     * @param data 数据的字节流
     * @return 校验和
     */
    byte getCheckSum(byte[] data);
}
