package com.star.counter.mapper;

import com.star.counter.bean.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface OrderMapper {
    /**
     * 通过用户id查询所有订单信息
     * @param uid 用户id
     * @return 用户的所有订单列表
     */
    List<OrderInfo> queryOrderByUid(long uid);

    /**
     * 将参数map的字段添加到t_order表中，同时传入的params中增加了id值
     * @param params 参数列表
     * @return 修改的记录数
     */
    int insertOrder(Map<String, Object> params);

    void updateOrder(long oid, int status);
}
