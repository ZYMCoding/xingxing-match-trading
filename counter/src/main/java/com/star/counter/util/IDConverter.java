package com.star.counter.util;

public class IDConverter {
    public static long combineIntToLong(int high, int low) {
        //int 4 位, long 8 位, 左移 4 * 8位变成long
        //高位+低位组合
        return ((long) high << 32 & 0xFFFFFFFF00000000L) | ((long) low & 0xFFFFFFFFL);
    }

    public static int[] seperateLongToInt(long val) {
        int[] res = new int[2];
        res[1] = (int) (0xFFFFFFFFL & val);   //低位
        res[0] = (int) ((0xFFFFFFFF00000000L & val) >> 32);  //高位
        return res;
    }
}
