package com.star.counter.service.api;

public interface BalanceService {

    Long getBalanceByUid(long uid);

    void addBalance(long uid, long balance);

    void minusBalance(long uid, long balance);
}
