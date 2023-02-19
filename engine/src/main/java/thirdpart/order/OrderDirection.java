package thirdpart.order;

import lombok.Getter;

@Getter
public enum OrderDirection {
    //从现金的角度来理解这方面的约定规则，0就是把钱花了，没钱
    BUY(0),
    SELL(1),

    PLUS_BALANCE(2),
    MINUS_BALANCE(3),

    OTHER(-1);//其他类型执行(撤单 等)

    private byte direction;

    OrderDirection(int direction) {
        this.direction = (byte) direction;
    }

    public static OrderDirection of(byte direction) {
        switch (direction) {
            case 0:
                return BUY;
            case 1:
                return SELL;
            case 2:
                return PLUS_BALANCE;
            case 3:
                return MINUS_BALANCE;
            case -1:
                return OTHER;
            default:
                throw new IllegalArgumentException("unknown OrderDirection:" + direction);
        }
    }

}
