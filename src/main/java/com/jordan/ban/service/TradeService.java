package com.jordan.ban.service;

import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class TradeService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Transactional
    public void trade(MockTradeResultIndex tradeResult) {
        if (tradeResult.getEatDiff() <= 0) {
            return;
        }
        TradeRecord record = new TradeRecord();
        Account accountA = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformA());
        Account accountB = this.accountRepository.findBySymbolAndPlatform(tradeResult.getSymbol(), tradeResult.getPlatformB());
        if (accountA == null || accountB == null) {
            throw new RuntimeException("Not found account!");
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
        this.accountRepository.save(accountA);
        this.accountRepository.save(accountB);
        this.tradeRecordRepository.save(record);
    }
}
