package com.star.seq.bean;


import lombok.*;
import thirdpart.order.OrderCmd;

import java.io.Serializable;
import java.util.List;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CmdPack implements Serializable {

    private long packNo;

    private List<OrderCmd> orderCmds;
}
