package com.star.counter.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 持仓信息
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class PosiInfo {
    private int id;
    private long uid;
    private int code;
    private String name;
    private long cost;
    private long count;
}
