package com.star.engine.mapper;

import org.apache.ibatis.annotations.MapKey;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

//@SuppressWarnings("MybatisXMapperMethodInspection")
public interface DBQuery {

    /**
     * 查询所有用户的资金信息
     * @return 用户的资金信息列表
     */
    List<Map<String, Object>> queryAllBalance();

    /**
     * 查询所有股票代码
     * @return 股票代码的集合
     */
    HashSet<Integer> queryAllStockCode();

    /**
     * 查询所有会员信息
     * @return 会员id的数组
     */
    List<Integer> queryAllMemberIds();
}
