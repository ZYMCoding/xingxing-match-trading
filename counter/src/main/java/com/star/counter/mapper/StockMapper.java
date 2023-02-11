package com.star.counter.mapper;

import com.star.counter.bean.StockInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface StockMapper {

    /**
     * 查询所有股票信息
     * @return 所有股票信息
     */
    List<StockInfo> queryAllStockInfo();
}
