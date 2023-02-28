import com.alipay.remoting.exception.CodecException;
import net.openhft.chronicle.core.util.Time;
import thirdpart.bean.CmdPack;
import thirdpart.codec.api.BodyCodec;
import thirdpart.codec.impl.BodyCodecImpl;
import thirdpart.order.CmdType;
import thirdpart.order.OrderCmd;
import thirdpart.order.OrderDirection;
import thirdpart.order.OrderType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class testBodyCodec {
    public static void main(String[] args) throws CodecException {
        Date date = new Date();
        long nowTime = date.getTime();
        OrderCmd orderCmd1 = OrderCmd.builder()
                .direction(OrderDirection.BUY)
                .mid((short) 12)
                .oid(123454L)
                .code(123)
                .price(120000L)
                .volume(120)
                .orderType(OrderType.LIMIT)
                .oid(123213L)
                .build();
        OrderCmd orderCmd2 = OrderCmd.builder()
                .direction(OrderDirection.BUY)
                .mid((short) 12)
                .oid(123454L)
                .code(123)
                .price(120000L)
                .volume(120)
                .orderType(OrderType.LIMIT)
                .oid(123213L)
                .build();
        List<OrderCmd> list = new ArrayList<>();
        list.add(orderCmd1);
        list.add(orderCmd2);
        CmdPack cmdPack = new CmdPack(1L, list);
        BodyCodec bodyCodec = new BodyCodecImpl();
        byte[] serialize = bodyCodec.serialize(cmdPack);
        CmdPack deserialize = bodyCodec.deserialize(serialize, CmdPack.class);
        System.out.println(deserialize);
    }
}
