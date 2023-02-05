import {constants} from './constants'

//股票代码
export const codeFormat = (code) => {
    // 1 -> 000001
    return ('000000' + code).slice(-6);
};

//资金
export const moneyFormat = (money) => {
    return (money / constants.MULTI_FACTOR).toFixed(2)
};

//委托方向
export const directionFormat = (direction) => {
    if (direction == constants.BUY) {
        return "买入";
    } else if (direction == constants.SELL) {
        return "卖出";
    } else {
        return "未知";
    }
};

//委托状态
export const statusFormat = (status) => {
    switch (status) {
        case constants.NOT_ORDER:
            return '未报';
        case constants.CANCELED:
            return '已撤';
        case constants.PART_CANCELED:
            return '部撤';
        case constants.ORDERED:
            return '已报';
        case constants.TRADED:
            return '已成';
        case constants.PART_TRADED:
            return '部成';
        case constants.ILLEGAL:
            return '废单';
        default:
            return '未知';
    }
};