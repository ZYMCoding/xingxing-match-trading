import {reqRealEnd, reqRealEndAsync} from './axiosCommon'

import {config} from './frontConfig'

import store from '../store/index'

//查资金
export const queryBalance = () => {
    reqRealEndAsync("post", config.real_domain,
        '/api/balance',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {
            // store.state.posiData = data;
            store.commit("updateBalance", data)
        })
};

//查持仓
export const queryPosi = () => {
    reqRealEndAsync("post", config.real_domain,
        '/api/posiinfo',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {
            // store.state.posiData = data;
            store.commit("updatePosi", data)
        })
};

//查股票代码
export const queryCodeName = (params) => {
    return reqRealEnd("post",
        config.real_domain, '/api/code', params);
};

//查委托
export const queryOrder = () => {
    reqRealEndAsync("post", config.real_domain,
        '/api/orderinfo',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {
            store.commit("updateOrder", data)
        })
};

//查成交
export const queryTrade = () => {
    reqRealEndAsync("post", config.real_domain,
        '/api/tradeinfo',
        {uid: sessionStorage.getItem('uid')},
        (code, msg, data) => {
            store.commit("updateTrade", data)
        })
};

//发送委托
export const sendOrder = (params,callback) =>{
    return reqRealEndAsync("post",config.real_domain,
        '/api/sendorder',params,callback);
}

// 委托
export const cancelOrder = (params,callback) => {
    return reqRealEndAsync("post", config.real_domain, '/api/cancelorder', params, callback);
};