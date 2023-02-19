package thirdpart.checksum.impl;

import thirdpart.checksum.api.CheckSum;

public class CheckSumImpl implements CheckSum {
    @Override
    public byte getCheckSum(byte[] data) {
        byte sum = 0;
        //将所有字节进行异或的累加
        //运算简单快速并且一旦任意字节修改，校验和会改变
        //改变字节数较多时有可能出现校验和相同的巧合
        for (byte b : data) {
            sum ^= b;
        }
        return sum;
    }
}
