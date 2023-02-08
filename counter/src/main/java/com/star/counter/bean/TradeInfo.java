package com.star.counter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 成交信息
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class TradeInfo {

    private int id;
    private long uid;
    private int code;
    private String name;
    private int direction;
    private long price;
    private long tcount;
    private int oid;
}
