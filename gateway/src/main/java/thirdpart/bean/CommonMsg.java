package thirdpart.bean;

import lombok.*;

import java.io.Serializable;

/**
 * 网关内外沟通用消息体
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CommonMsg implements Serializable {

    //    包头[ 包体长度 int + 校验和 byte + src short+ dst short + 消息类型 short + 消息状态 byte + 包编号 long ]
    //    包体[ 数据 byte[] ]

    /**
     * 包体长度
     */
    private int bodyLength;

    /**
     * 校验和
     */
    private byte checksum;

    /**
     * 数据源
     */
    private short msgSrc;

    /**
     * 数据目的地
     */
    private short msgDst;

    /**
     * 消息类型
     */
    private short msgType;

    /**
     * 消息状态
     */
    private byte status;

    /**
     * 包编号(UUID)
     */
    private long msgNo;

    /**
     * 包体
     */
    @ToString.Exclude
    private byte[] body;


    ////////////////////////////////
    private boolean isLegal;

    private short errCode;

    private long timestamp;


}
