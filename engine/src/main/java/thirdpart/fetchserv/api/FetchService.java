package thirdpart.fetchserv.api;

import thirdpart.order.OrderCmd;

import java.util.List;

public interface FetchService {
    List<OrderCmd> fetchData();
}
