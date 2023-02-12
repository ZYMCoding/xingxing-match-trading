package com.star.counter.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface BalanceMapper {

    /**
     * 通过uid获取账户资金
     * @param uid 用户id
     * @return 用户资金
     */
    Long queryBalanceByUid(long uid);

    /**
     * 增加用户的资金
     * @param uid 用户id
     * @param balance 需要增加的资金
     */
    void addBalance(long uid, long balance);
}
