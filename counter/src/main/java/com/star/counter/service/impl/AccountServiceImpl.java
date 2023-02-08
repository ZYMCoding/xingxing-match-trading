package com.star.counter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.counter.bean.Account;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.mapper.AccountMapper;
import com.star.counter.service.api.AccountService;
import com.star.counter.util.SnowflakeIdWorker;
import com.star.counter.util.TimeformatUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    RedisStringCache redisStringCache;

    @Autowired
    AccountMapper accountMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public Account login(long uid, String password, String captcha, String captchaId) throws JsonProcessingException {
        //检验非空，防止无效查询
        if (StringUtils.isAnyBlank(password, captcha, captchaId)) {
            return null;
        }
        //获取验证码
        String captchaCache = redisStringCache.get(captchaId, CacheType.CAPTCHA);
        if (StringUtils.isEmpty(captchaCache)) {
            //传过来的验证码为空
            return null;
        } else if (!StringUtils.equalsAnyIgnoreCase(captcha, captchaCache)) {
            //验证码与缓存中查询到的不一致
            return null;
        }
        //检验账户id和密码信息
        Account account = accountMapper.queryAccount(uid, password);
        if (account == null) {
            return null;
        } else {
            //增加唯一ID作为身份认证标志
            account.setToken(String.valueOf(snowflakeIdWorker.nextId()));
            ObjectMapper objectMapper = new ObjectMapper();
            //存入缓存,key为标识符,value为account对应的json
            redisStringCache.cache(account.getToken(), objectMapper.writeValueAsString(account), CacheType.ACCOUNT);
            //更新登陆时间
            Date date = new Date();
            accountMapper.updateAccountLoginTime(uid, TimeformatUtil.yyyyMMdd(date), TimeformatUtil.hhMMss(date));
            return account;
        }
    }

    @Override
    public boolean accountExistInCache(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        //从缓存获取数据
        String accountJson = redisStringCache.get(token, CacheType.ACCOUNT);
        if (accountJson != null) {
            //重新激活缓存(增加有效期)
            redisStringCache.cache(token, accountJson, CacheType.ACCOUNT);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void logout(String token) {
        redisStringCache.remove(token, CacheType.ACCOUNT);
    }

    @Override
    public boolean updatePassword(long uid, String oldPwd, String newPwd) {
        int res = accountMapper.updateAccountPassword(uid, oldPwd, newPwd);
        return res != 0;
    }
}
