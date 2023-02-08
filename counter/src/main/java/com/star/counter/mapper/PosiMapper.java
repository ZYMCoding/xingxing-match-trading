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
}
