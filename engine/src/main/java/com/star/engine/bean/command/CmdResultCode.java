package com.star.engine.bean.command;

import lombok.Getter;

@Getter
public enum CmdResultCode {

    SUCCESS(1),


    /////////////////////////orderbook/////////////////////////////

    INVALID_ORDER_ID(-1),//不合法委托ID
    INVALID_ORDER_PRICE(-2),//不合法委托价格
    DUPLICATE_ORDER_ID(-3),//重复委托编号
    UNKNOWN_MATCH_CMD(-4),//未知撮合指令
    INVALID_ORDER_BOOK_ID(-5),//未知订单簿

    ///////////////////////risk/////////////////////////////
    RISK_INVALID_USER(-100),//用户不存在
    RISK_INVALID_CODE(-101),//代码不存在
    RISK_INVALID_BALANCE(-102),//资金不正确

    ///////////////////////match/////////////////////////////
    MATCHING_INVALID_STOCK(-200),

    //    VALID_FOR_MATCHING_ENGINE(1),
    //
    //    SUCCESS(100),
    //    ACCEPTED(110),
    //
    //    AUTH_INVALID_USER(-1001),
    //    AUTH_TOKEN_EXPIRED(-1002),
    //
    //    INVALID_SYMBOL(-1201),
    //    INVALID_PRICE_STEP(-1202),
    //
    //    RISK_NSF(-2001),
    //
    //    MATCHING_UNKNOWN_ORDER_ID(-3002),
    //    MATCHING_DUPLICATE_ORDER_ID(-3003),
    //    MATCHING_UNSUPPORTED_COMMAND(-3004),
    //    INVALID_ORDER_BOOK_ID(-3005),
    //    MATCHING_ORDER_BOOK_ALREADY_EXISTS(-3006),
    ////    MATCHING_MOVE_REJECTED_DIFFERENT_PRICE(-3040),
    //    MATCHING_MOVE_FAILED_PRICE_OVER_RISK_LIMIT(-3041),
    //
    //    USER_MGMT_USER_ALREADY_EXISTS(-4001),
    //
    ////    USER_MGMT_ACCOUNT_BALANCE_ADJUSTMENT_ZERO(-4100),
    //    USER_MGMT_ACCOUNT_BALANCE_ADJUSTMENT_ALREADY_APPLIED_SAME(-4101),
    //    USER_MGMT_ACCOUNT_BALANCE_ADJUSTMENT_ALREADY_APPLIED_MANY(-4102),
    //    USER_MGMT_ACCOUNT_BALANCE_ADJUSTMENT_NSF(-4103),
    //    USER_MGMT_NON_ZERO_ACCOUNT_BALANCE(-4104),
    //
    //    USER_MGMT_USER_NOT_SUSPENDABLE_HAS_POSITIONS(-4130),
    //    USER_MGMT_USER_NOT_SUSPENDABLE_NON_EMPTY_ACCOUNTS(-4131),
    //    USER_MGMT_USER_NOT_SUSPENDED(-4132),
    //    USER_MGMT_USER_ALREADY_SUSPENDED(-4133),
    //
    //    USER_MGMT_USER_NOT_FOUND(-4201),
    //
    //    SYMBOL_MGMT_SYMBOL_ALREADY_EXISTS(-5001),
    //
    //    BINARY_COMMAND_FAILED(-8001),
    //    STATE_HASH_FAILED(-8003),
    //    STATE_PERSIST_RISK_ENGINE_FAILED(-8010),
    //    STATE_PERSIST_MATCHING_ENGINE_FAILED(-8020),

    DROP(-9999);

    // codes below -10000 are reserved for gateways


    private int code;

    CmdResultCode(int code) {
        this.code = code;
    }

}

