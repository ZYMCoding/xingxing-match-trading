package com.star.counter.mapper;

import com.star.counter.bean.PosiInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PosiMapper {

    /**
     * 通过用户id查询持仓信息
     * @param uid 用户id
     * @return 该用户对应的持仓列表
     */
    List<PosiInfo> queryPosiByUid(long uid);

    /**
     * 增加持仓
     * @param uid 用户id
     * @param code 增加的持仓股的股票代码
     * @param volume 增加量
     * @param price 价格
     */
    void addPosi(long uid, int code, long volume, long price);

    /**
     * 查询某用户对某股票的持仓信息
     * @param uid 用户id
     * @param code 股票代码
     * @return 查到uid的用户对code股票的持仓信息
     */
    PosiInfo queryOnePosi(long uid, int code);

    /**
     * 增加持仓信息
     * @param uid 用户id
     * @param code 股票代码
     * @param volume 交易量
     * @param price 购买价格
     */
    void insetPosi(long uid, int code, long volume, long price);
}
