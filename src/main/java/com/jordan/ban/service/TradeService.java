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

@Service
@Slf4j
public class TradeService {

    private static double GATEIO_HUOBI_EOS_USDT_METRICS_MAX = 0.01; // 0.4%

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    private volatile double lastTradeVolume = 0;


    public synchronized void trade(MockTradeResultIndex tradeResult) {
        if (lastTradeVolume == tradeResult.getEatTradeVolume()) {
            log.info(String.format("Last trade volume=%s, do nothing!", tradeResult.getEatTradeVolume()));
            return;
        }
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
        record.setAccountB(accountB.getID());
        record.setDirect(tradeResult.getTradeDirect());
        record.setEatDiff(tradeResult.getEatDiff());
        record.setEatDiffPercent(tradeResult.getEatPercent());
//        record.setPrice();
        record.setSymbol(tradeResult.getSymbol());
        record.setTradeTime(tradeResult.getCreateTime());
        record.setVolume(tradeResult.getEatTradeVolume());
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() - tradeResult.getEatTradeVolume());
            accountA.setMoney(accountA.getMoney() - tradeResult.getBuyCost());
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() + tradeResult.getEatTradeVolume());
            accountB.setMoney(accountB.getMoney() + tradeResult.getSellCost());
        } else {
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() - tradeResult.getEatTradeVolume());
            accountB.setMoney(accountB.getMoney() - tradeResult.getBuyCost());
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() + tradeResult.getEatTradeVolume());
            accountA.setMoney(accountA.getMoney() + tradeResult.getSellCost());
        }
        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        if (accountA.getMoney() < 0 || accountB.getMoney() < 0) {
            log.info("Not enough money!");
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("Not enough coin!");
            return;
        }
        double totalMoney = accountA.getMoney() + accountB.getMoney();
        double profit = ((moneyAfter - moneyBefore) / moneyBefore) * 100;
        log.info("Profit:{}", profit);
        if (profit < 0) {
            if (coinDiffAfter < coinDiffBefore) {
                if (Math.abs(profit) <= GATEIO_HUOBI_EOS_USDT_METRICS_MAX * 0.5) {
                    //往回搬;
                    log.info("Move back!");
                } else {
                    log.info("Right way. but not enough profit. Not deal!");
                    return;
                }
            } else {
                log.info("Not coin balance! Not Deal!");
                return;
            }
        } else {
            // 有利润，但是不够
            if (profit < GATEIO_HUOBI_EOS_USDT_METRICS_MAX) {
                log.info("Profit:{}, Not deal!", profit);
                return;
            }
        }
        record.setProfit(profit);
        record.setTotalProfit(((totalMoney - AccountService.USD_MONEY) / AccountService.USD_MONEY) * 100);
        record.setTotalMoney(totalMoney);
        this.accountRepository.save(accountA);
        this.accountRepository.save(accountB);
        this.tradeRecordRepository.save(record);
        lastTradeVolume = tradeResult.getEatTradeVolume();
        log.info("Traded !!!!!!!!!!!");
    }
}
