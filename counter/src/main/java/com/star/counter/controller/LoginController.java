package com.star.counter.controller;

import com.star.counter.bean.res.CaptchaRes;
import com.star.counter.bean.res.CounterRes;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.util.Captcha;
import com.star.counter.util.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    RedisStringCache redisStringCache;

    @RequestMapping("/captcha")
    @ResponseBody
    public CounterRes captcha() throws IOException {
        //生成验证码120px * 40px(4个字符)
        Captcha captcha = new Captcha(120, 40, 4, 10);

        //将验证码的ID，数值放入缓存
        String uuid = String.valueOf(snowflakeIdWorker.nextId());
        redisStringCache.cache(uuid, captcha.getCode(), CacheType.CAPTCHA);

        //使用base64编码图片并返回给前台,返回uuid和base64
        CaptchaRes res = new CaptchaRes(uuid, captcha.getBase64ByteStr());
        return new CounterRes(res);
    }
}
