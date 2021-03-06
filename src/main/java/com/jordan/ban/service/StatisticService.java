package com.jordan.ban.service;

import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.jordan.ban.common.Constant.USDT;

@Service
public class StatisticService {


    @Autowired
    private AccountService accountService;

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    @Autowired
    private SlackService slackService;

    public void statistic(String marketA, String marketB, String symbol, String coinName) {
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(marketA);
        AccountDto accountA = AccountDto.builder().money(balanceA.get(USDT.toLowerCase()).getBalance()).platform(marketA).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getBalance() : 0).build();
        Map<String, BalanceDto> balanceB = accountService.findBalancesCache(marketB);
        AccountDto accountB = AccountDto.builder().money(balanceB.get(USDT.toLowerCase()).getBalance()).platform(marketB).symbol(symbol)
                .virtualCurrency(balanceB.get(coinName) != null ? balanceB.get(coinName).getBalance() : 0).build();

        ProfitStatistics before = profitStatisticsRepository.findTopBySymbolAndAndPlatformAAndAndPlatformBOrderByCreateTimeDesc(symbol, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME);
        double moneyBefore, increase, increasePercent, moneyAfter, coinAfter;
        moneyAfter = accountA.getMoney() + accountB.getMoney();
        coinAfter = accountA.getVirtualCurrency() + accountB.getVirtualCurrency();
        if (before != null) {
            moneyBefore = before.getSumMoney();
        } else {
            moneyBefore = moneyAfter;
        }
        increase = moneyAfter - moneyBefore;
        increasePercent = (increase / Math.min(moneyAfter, moneyBefore)) * 100;
        ProfitStatistics after = ProfitStatistics.builder().cycleType(CycleType.day).symbol(symbol)
                .increase(increase).sumCoin(coinAfter).sumMoney(moneyAfter)
                .increasePercent(increasePercent).platformA(accountA.getPlatform())
                .platformB(accountB.getPlatform()).build();
        this.profitStatisticsRepository.save(after);
        slackService.sendMessage("Statistic Profit", after.toString());
    }
}
