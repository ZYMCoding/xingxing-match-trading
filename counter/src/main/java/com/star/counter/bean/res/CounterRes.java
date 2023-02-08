package com.star.counter.bean.res;

/**
 * 通用返回格式
 */
public class CounterRes {

    public static final int SUCCESS = 0;
    public static final int RELOGIN = 1;
    public static final int FAIL = 2;

    private int code;

    private String message;

    private Object data;

    public CounterRes() {}

    public CounterRes(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public CounterRes(Object data) {
        this(SUCCESS, "", data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
