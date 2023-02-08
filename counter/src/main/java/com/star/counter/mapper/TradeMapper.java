package com.star.counter.mapper;

import com.star.counter.bean.TradeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface TradeMapper {


    /**
     * 通过用户id查询成交信息
     * @param uid 用户id
     * @return 用户的成交信息列表
     */
    List<TradeInfo> queryTradeByUid(long uid);
}
