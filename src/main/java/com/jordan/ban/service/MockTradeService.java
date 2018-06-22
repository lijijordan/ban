package com.jordan.ban.service;

import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AopInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MockTradeService {

    private static double DEFAULT_METRICS_MAX = 0.003; // 0.4%

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    private Map<String, Double> lastTradeMap = new ConcurrentHashMap<>();

    /**
     * 模拟买卖，计算收益
     *
     * @param tradeResult
     */
    public synchronized void mockTrade(MockTradeResultIndex tradeResult) {
        String lastTradeKey = tradeResult.getSymbol() + tradeResult.getDiffPlatform() + tradeResult.getTradeDirect();
        if (this.lastTradeMap.get(lastTradeKey) != null && this.lastTradeMap.get(lastTradeKey) == tradeResult.getEatTradeVolume()) {
            log.info(String.format("Last trade volume=%s, do nothing!", tradeResult.getEatTradeVolume()));
            return;
        }
        lastTradeMap.put(lastTradeKey, tradeResult.getEatTradeVolume());
        // Market A
        Account accountA = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformA());
        Account accountB = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformB());
        if (accountA == null || accountB == null) {
            log.info("Not found account!");
            return;
        }
        double coinDiffBefore = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyBefore = accountA.getMoney() + accountB.getMoney();
        TradeRecord record = new TradeRecord();
        log.info(tradeResult.toString());
        record.setAccountA(accountA.getID());
        record.setPlatformA(accountA.getPlatform());
        record.setAccountB(accountB.getID());
        record.setPlatformB(accountB.getPlatform());
        record.setDirect(tradeResult.getTradeDirect());
        record.setEatDiff(tradeResult.getEatDiff());
        record.setEatDiffPercent(tradeResult.getEatPercent());
//        record.setPrice();
        record.setSymbol(tradeResult.getSymbol());
        record.setTradeTime(tradeResult.getCreateTime());
        record.setVolume(tradeResult.getEatTradeVolume());
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() + tradeResult.getEatTradeVolume());
            accountA.setMoney(accountA.getMoney() - tradeResult.getBuyCost());
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() - tradeResult.getEatTradeVolume());
            accountB.setMoney(accountB.getMoney() + tradeResult.getSellCost());
        } else {  // 市场B买. 市场A卖
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() + tradeResult.getEatTradeVolume());
            accountB.setMoney(accountB.getMoney() - tradeResult.getBuyCost());
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() - tradeResult.getEatTradeVolume());
            accountA.setMoney(accountA.getMoney() + tradeResult.getSellCost());
        }
        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();
        Double avgEatDiffPercent = this.tradeRecordRepository.avgEatDiffPercent(accountA.getID(), accountB.getID(), tradeResult.getSymbol());
        if (avgEatDiffPercent == null) {
            avgEatDiffPercent = DEFAULT_METRICS_MAX;
        }
        if (accountA.getMoney() < 0 || accountB.getMoney() < 0) {
            log.info("Not enough money!");
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("Not enough coin!");
            return;
        }
        double totalMoney = accountA.getMoney() + accountB.getMoney();
        if (diffPercent < 0) {  // 亏损
            if (coinDiffAfter < coinDiffBefore) {
                if (Math.abs(diffPercent) <= avgEatDiffPercent * 0.8) {
                    //往回搬;
                    log.info("Move back!");
                } else {
                    log.info("Right way. but not enough profit. Not deal!");
                    return;
                }
            } else {
                log.info("Coin won't balance! Not Deal!");
                return;
            }
        } else {
            // 有利润
            if (diffPercent < avgEatDiffPercent) {
                if (coinDiffAfter < coinDiffBefore) { // 方向正确
                    //往回搬;
                    log.info("Move back! Earn a little.");
                } else { // 方向错误
                    log.info("Coin won't balance! Not Deal!");
                    return;
                }
            }
        }
        double profit = ((moneyAfter - moneyBefore) / moneyBefore) * 100;
        log.info("Profit:{}", profit);
        record.setProfit(profit);
        record.setTotalProfit(((totalMoney - AccountService.USD_MONEY) / AccountService.USD_MONEY) * 100);
        record.setTotalMoney(totalMoney);
        this.accountRepository.save(accountA);
        this.accountRepository.save(accountB);
        this.tradeRecordRepository.save(record);
        log.info("Traded !!!!!!!!!!!");
    }
}
