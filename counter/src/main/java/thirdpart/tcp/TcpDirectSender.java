package thirdpart.tcp;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Log4j2
@RequiredArgsConstructor
public class TcpDirectSender {

    @NonNull
    private String ip;

    @NonNull
    private int port;

    @NonNull
    private Vertx vertx;

    public void startUp() {
        vertx.createNetClient().connect(port, ip, new ClientConnHandler());
        //线程轮询缓存，缓存中有数据便发送
        new Thread(() -> {
            while (true) {
                try {
                    //阻塞超过5秒则会返回null
                    Buffer msgBuffer = sendCache.poll(5, TimeUnit.SECONDS);
                    if (msgBuffer != null && msgBuffer.length() > 0 && socket != null) {
                        socket.write(msgBuffer);
                    }
                } catch (Exception e) {
                    log.error("msg send fail, continue");
                }
            }
        });
    }

    public boolean send(Buffer bufferMsg) {
        return sendCache.offer(bufferMsg);
    }

    //volatile的作用是每次从内存中取到最新的
    private volatile NetSocket socket;

    private class ClientConnHandler implements Handler<AsyncResult<NetSocket>> {
        @Override
        public void handle(AsyncResult<NetSocket> result) {
            if (result.succeeded()) {
                log.info("connect success to remote {}: {}", ip, port);
                socket = result.result();
                //关闭处理器
                socket.closeHandler(close -> {
                    log.info("connect to remote {} closed", socket.remoteAddress());
                    //重连
                    reconnect();
                });
            } else {
            }
        }

        private void reconnect() {
            vertx.setTimer(5000, r -> {
               log.info("try reconnect to server to {}: {} failed", ip, port);
               vertx.createNetClient().connect(port, ip, new ClientConnHandler());
            });
        }
    }

    //缓存队列,每次让socket从缓存中获取数据
    private final BlockingQueue<Buffer> sendCache = new LinkedBlockingDeque<>();


}
