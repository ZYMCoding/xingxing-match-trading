package com.star.counter.service.impl;

import com.star.counter.mapper.BalanceMapper;
import com.star.counter.service.api.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    BalanceMapper balanceMapper;

    @Override
    public Long getBalanceByUid(long uid) {
        Long balance = balanceMapper.queryBalanceByUid(uid);
        return balance;
    }

    @Override
    public void addBalance(long uid, long balance) {
        balanceMapper.addBalance(uid, balance);
    }

    @Override
    public void minusBalance(long uid, long balance) {
        balanceMapper.addBalance(uid, -balance);
    }
}
