import {reqRealEnd, reqRealEndAsync} from './axiosCommon'

import {config} from './frontConfig'

import router from '../router'

//请求验证码
export const queryCaptcha = (callback) => {
    
    return reqRealEndAsync("post", config.real_domain,
        "/login/captcha", {}, callback);
};

// 登录
export const login = (params, callback) => {
    return reqRealEndAsync("post", config.real_domain,
        "/login/userlogin", params, callback);
};

//退出登录
export const logout = () =>{
    //移除登录信息
    sessionStorage.removeItem("uid");
    sessionStorage.removeItem("token");
    //跳转登录页面
    router.replace({
        path: "/",
        query: {
            msg: "成功退出"
        }
    });

    //通知柜台
    reqRealEnd("post",config.real_domain,'/login/logout',{});

}


export const pwdUpdate = (params,callback) =>{
    return reqRealEndAsync("post",config.real_domain,
        '/login/pwdupdate',params,callback);
}








