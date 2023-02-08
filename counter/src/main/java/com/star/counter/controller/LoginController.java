package com.star.counter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.star.counter.bean.Account;
import com.star.counter.bean.res.CaptchaRes;
import com.star.counter.bean.res.CounterRes;
import com.star.counter.cache.CacheType;
import com.star.counter.cache.RedisStringCache;
import com.star.counter.service.api.AccountService;
import com.star.counter.util.Captcha;
import com.star.counter.util.SnowflakeIdWorker;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisStringCache redisStringCache;

    @Autowired
    private AccountService accountService;

    /**
     * 生成验证码并返回验证码图片和uuid,同时将uuid和对应的验证码值存入Redis
     * @return 包含uuid和验证码图片的CounterRes对象
     */
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

    @RequestMapping("/userlogin")
    @ResponseBody
    public CounterRes login(@RequestParam long uid,
                            @RequestParam String password,
                            @RequestParam String captcha,
                            @RequestParam String captchaId) throws JsonProcessingException {
        Account account = accountService.login(uid, password, captcha, captchaId);
        if (account == null) {
            return new CounterRes(CounterRes.FAIL, "用户名密码/验证码错误，登陆失败", null);
        } else {
            return new CounterRes(account);
        }
    }

    @RequestMapping("/loginfail")
    @ResponseBody
    public CounterRes loginFail() {
        return new CounterRes(CounterRes.RELOGIN, "请重新登录", null);
    }

    @RequestMapping("logout")
    @ResponseBody
    public CounterRes logout(@RequestParam String token) {
        accountService.logout(token);
        return new CounterRes(CounterRes.SUCCESS, "退出成功", null);
    }

    @RequestMapping("/pwdupdate")
    @ResponseBody
    public CounterRes pwdUpdate(@RequestParam long uid,
                                @RequestParam String oldPwd,
                                @RequestParam String newPwd) {
        boolean res = accountService.updatePassword(uid, oldPwd, newPwd);
        if (res) {
            return new CounterRes(CounterRes.SUCCESS, "密码更新成功", null);
        } else {
            return new CounterRes(CounterRes.FAIL, "密码更新失败", null);
        }
    }
}
