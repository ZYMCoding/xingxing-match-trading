package thirdpart.bus.api;

import thirdpart.bean.CommonMsg;

public interface BusSender {

    void startUp();

    void publish(CommonMsg commonMsg);
}
