package com.jordan.ban.service;

import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.aop.AopInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private static double GATEIO_HUOBI_EOS_USDT_METRICS_MAX = 0.01613;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    private volatile double lastTradeVolume = 0;


    public synchronized void trade(MockTradeResultIndex tradeResult) {
        if (lastTradeVolume == tradeResult.getEatTradeVolume()) {
            System.out.println(String.format("Last trade volume=%s, do nothing!", tradeResult.getEatTradeVolume()));
            return;
        }
        Account accountA = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformA());
        Account accountB = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformB());

        if (tradeResult.getEatDiff() <= 0) {
            double avgEatDiffPer = 0;
            try {
                avgEatDiffPer = this.tradeRecordRepository.avgEatDiffPercent(accountA.getID(), accountB.getID(), tradeResult.getSymbol());
            } catch (AopInvocationException e) {
            }
            if (accountA.getVirtualCurrency() > accountB.getVirtualCurrency()) { // A 有币、B、没币
                if (tradeResult.getTradeDirect() == TradeDirect.A2B) { //币 B->A
                    // 退出
                    System.out.println(String.format("币:B->A,Diff=%s, wrong way .do nothing!", tradeResult.getEatPercent()));
                    return;
                } else {//币 A->B （Right way）
                    // 损失大于平均收益
                    if (Math.abs(tradeResult.getEatPercent()) > avgEatDiffPer) {
                        System.out.println(String.format("币:A->B,Diff=%s, AvgDiff=%s .do nothing!", tradeResult.getEatPercent(), avgEatDiffPer));
                        return;
                    }
                }
            } else {  // A 没币、B、有币
                if (tradeResult.getTradeDirect() == TradeDirect.B2A) { //币 A->B
                    // 退出
                    System.out.println(String.format("币:A->B,Diff=%s, wrong way .do nothing!", tradeResult.getEatPercent()));
                    return;
                } else {//币 B->A （Right way）
                    // 损失大于平均收益
                    if (Math.abs(tradeResult.getEatPercent()) > avgEatDiffPer) {
                        System.out.println(String.format("币:B->A,Diff=%s, AvgDiff=%s .do nothing!", tradeResult.getEatPercent(), avgEatDiffPer));
                        return;
                    }
                }
            }
        } else if (tradeResult.getEatDiff() < GATEIO_HUOBI_EOS_USDT_METRICS_MAX / 2) {
            System.out.println("Not reached GATEIO_HUOBI_EOS_USDT_METRICS_MAX value!!");
            return;
        }

        TradeRecord record = new TradeRecord();
        System.out.println(tradeResult.toString());
        if (accountA == null || accountB == null) {
            System.out.println("Not found account!");
            return;
        }
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
        if (accountA.getMoney() < 0 || accountB.getMoney() < 0) {
            System.out.println("Not enough money!");
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            System.out.println("Not enough coin!");
            return;
        }
        record.setTotalMoney(accountA.getMoney() + accountB.getMoney());
        record.setProfit(((record.getTotalMoney() - AccountService.USD_MONEY) / AccountService.USD_MONEY) * 100);
        this.accountRepository.save(accountA);
        this.accountRepository.save(accountB);
        this.tradeRecordRepository.save(record);
        lastTradeVolume = tradeResult.getEatTradeVolume();
        System.out.println("Traded !!!!!!!!!!!");
    }
}
