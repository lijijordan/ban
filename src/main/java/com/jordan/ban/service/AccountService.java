package com.jordan.ban.service;

import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.BalanceRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.dao.WareHouseRepository;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.entity.Account;
import com.jordan.ban.market.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jordan.ban.common.Constant.*;

@Service
@Slf4j
public class AccountService {

    private Map<String, Map<String, BalanceDto>> balanceCache = new ConcurrentHashMap<>();
    public static double USD_MONEY = 1000;

    /**
     * Find all currency balances.
     *
     * @param platformName
     * @return
     */
    // TODO:use cache
    public Map<String, BalanceDto> refreshBalancesCache(String platformName) {
        log.info("update {} balances!", platformName);
        Map<String, BalanceDto> map = new HashMap<>();
        MarketParser market = MarketFactory.getMarket(platformName);
        if (market instanceof Huobi) {
            Huobi huobi = (Huobi) market;
            long accountId = huobi.getAccountID();
            huobi.getBalances(accountId).forEach(balanceDto -> {
                map.put(balanceDto.getCurrency(), balanceDto);
            });
        } else if (market instanceof Fcoin) {
            ((Fcoin) market).getBalances().forEach(balanceDto -> {
                map.put(balanceDto.getCurrency(), balanceDto);
            });
        } else if (market instanceof Dragonex) {
            ((Dragonex) market).getBalances().forEach(balanceDto -> {
                map.put(balanceDto.getCurrency(), balanceDto);
            });
        }
        if (!map.isEmpty()) {
            synchronized (balanceCache) {
                log.info("update balance cache,{}:{}", platformName, map);
                balanceCache.put(platformName, map);
            }
        }
        return map;
    }

    public Map<String, BalanceDto> findBalancesCache(String platformName) {
        Map<String, BalanceDto> map;
        synchronized (balanceCache) {
            map = balanceCache.get(platformName);
        }
        if (map == null) {
            map = refreshBalancesCache(platformName);
        }
        return map;
    }
}
