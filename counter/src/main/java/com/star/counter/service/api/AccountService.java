package com.star.counter.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.Account;
import org.springframework.stereotype.Service;

public interface AccountService {

    /**
     * 登录
     * @param uid 用户id
     * @param password 密码
     * @param captcha 验证码
     * @param captchaId 验证码Id(UUID)
     * @return 查询到的用户对象
     */
    Account login(long uid, String password, String captcha, String captchaId) throws JsonProcessingException;

    /**
     * 是否存在登录信息(身份校验)
     * @param token 用户的标识符
     */
    boolean accountExistInCache(String token);

    /**
     * 清除token信息，退出登录
     * @param token 用户的标识符
     */
    void logout(String token);

    /**
     * 更新密码
     * @param uid 用户id
     * @param oldPwd 老密码
     * @param newPwd 新密码
     * @return 是否修改成功
     */
    boolean updatePassword(long uid, String oldPwd, String newPwd);
}
