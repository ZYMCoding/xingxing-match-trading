import {reqRealEndAsync} from './axiosCommon';

import {config} from './frontConfig'

export const queryTransfer = (params,callback) =>{
    return reqRealEndAsync("post",config.real_domain,
        '/api/transferquery',params,callback);
};