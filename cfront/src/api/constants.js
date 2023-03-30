export const constants = {

    //乘数
    MULTI_FACTOR: 10000,

    //委托类型
    NEW_ORDER: 0,
    CANCEL_ORDER: 1,

    //价格类型
    LIMIT: 0,
    MARKET: 1,

    //买卖方向
    BUY: 0,
    SELL: 1,

    //委托状态
    //未报，撤单，部分撤单，已报，成交，部分成交，废单
    NOT_ORDER: -1,
    CANCELED: 1,
    PART_CANCELED: 2,
    ORDERED: 3,
    TRADED: 4,
    PART_TRADED: 5,
    ILLEGAL: 6,

};
