package thirdpart.order;

public enum CmdType {

    ///////////////委托类//////////////
    NEW_ORDER(0),
    CANCEL_ORDER(1),

    //////权限类(交易所几乎没有用过,都是盘后改，初始化的时候载入新数据)////////
    SUSPEND_USER(2),
    RESUME_USER(3),

    ///////////////状态类//////////////
    SHUTDOWN_ENGINE(4),

    ///////////////查询类//////////////
    BINARY_DATA(5),
    ORDER_BOOK_REQUEST(6),

    ///////////////行情类//////////////
    HQ_PUB(7),


    ///////////////资金类//////////////
    BALANCE_ADJUSTMENT(8);

    private short type;

    CmdType(int type) {
        this.type = (short) type;
    }

    public static CmdType of(short type) {
        //柜台只能处理三种
        switch (type) {
            case 0:
                return NEW_ORDER;
            case 1:
                return CANCEL_ORDER;
            case 8:
                return BALANCE_ADJUSTMENT;
            default:
                throw new IllegalArgumentException("unknown CmdType:" + type);
        }
    }
}
