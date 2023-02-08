package com.star.counter.mapper;

import com.star.counter.bean.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderMapper {
    List<OrderInfo> queryOrderByUid(long uid);
}
