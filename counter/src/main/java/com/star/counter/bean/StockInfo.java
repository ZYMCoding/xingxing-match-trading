package com.star.counter.bean;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class StockInfo {

    private int code;
    private String name;
    private String abbrName;
}
