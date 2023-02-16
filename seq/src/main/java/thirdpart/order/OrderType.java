package thirdpart.order;

import lombok.Getter;

@Getter
public enum OrderType {
    LIMIT(0); // Immediate or Cancel - equivalent to strict-risk market order

    private byte type;

    OrderType(int type) {
        this.type = (byte) type;
    }

    public static OrderType of(byte type) {
        switch (type) {
            case 0:
                return LIMIT;
            default:
                throw new IllegalArgumentException("unknown OrderType:" + type);
        }
    }

}
