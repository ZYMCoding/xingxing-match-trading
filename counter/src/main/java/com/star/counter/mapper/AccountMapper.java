package com.star.counter.mapper;

import com.star.counter.bean.Account;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface AccountMapper {

    /**
     * 查询账户对象
     * @param uid 用户id
     * @param password 密码
     * @return 根据账号密码查询的对象
     */
    public Account queryAccount(long uid, String password);


    /**
     * 更新用户最后登录时间信息
     * @param uid 用户id
     * @param nowDate 当前时期
     * @param nowTime 当前时间
     */
    public void updateAccountLoginTime(long uid, String nowDate, String nowTime);

    /**
     * 修改密码
     * @param uid 用户id
     * @param oldPwd 老密码
     * @param newPwd 新密码
     */
    public int updateAccountPassword(long uid, String oldPwd, String newPwd);
}
