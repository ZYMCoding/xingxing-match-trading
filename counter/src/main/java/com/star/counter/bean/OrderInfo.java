package com.star.counter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 委托信息
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class OrderInfo {
    private int id;
    private long uid;
    private int code;
    private String name;
    private int direction;
    private int type;
    private long price;
    private long ocount;
    private int status;
    private String date;
    private String time;
}
