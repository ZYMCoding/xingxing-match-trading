package com.star.counter.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisStringCache {

    @Autowired
    private StringRedisTemplate template;

    @Value("${cacheexpire.captcha}")
    private int captchaExpireTime;

    @Value("${cacheexpire.account}")
    private int accountExpireTime;

    @Value("${cacheexpire.order}")
    private int orderExpireTime;

    public int getCaptchaExpireTime() {
        return captchaExpireTime;
    }

    public void setCaptchaExpireTime(int captchaExpireTime) {
        this.captchaExpireTime = captchaExpireTime;
    }

    public int getAccountExpireTime() {
        return accountExpireTime;
    }

    public void setAccountExpireTime(int accountExpireTime) {
        this.accountExpireTime = accountExpireTime;
    }

    public int getOrderExpireTime() {
        return orderExpireTime;
    }

    public void setOrderExpireTime(int orderExpireTime) {
        this.orderExpireTime = orderExpireTime;
    }

    //增加缓存
    public void cache(String key, String value, CacheType cacheType) {
        int expireTime;
        switch (cacheType) {
            case ACCOUNT:
                expireTime = getAccountExpireTime();
                break;
            case CAPTCHA:
                expireTime = getCaptchaExpireTime();
                break;
            case ORDER:
            case TRADE:
            case POSI:
                expireTime = getOrderExpireTime();
                break;
            default:
                expireTime = 10;
        }
        template.opsForValue().set(cacheType.type() + key, value, expireTime, TimeUnit.SECONDS);
    }

    //查询缓存
    public String get(String key, CacheType cacheType) {
        return template.opsForValue().get(cacheType.type() + key);
    }

    //删除缓存
    public void remove(String key, CacheType cacheType) {
        template.delete(cacheType.type() + key);
    }
}
