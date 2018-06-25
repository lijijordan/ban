package com.jordan.ban.task;

import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.jordan.ban.common.Constant.USDT;

@Slf4j
@Component
public class StatisticTask {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    @Autowired
    private SlackService slackService;

    /**
     * 每天中午十二点触发
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void statisticDragonexVSHuobiProfit() {
        String symbol = "ethusdt";
        String coinName = "eth";
        String marketA = Dragonex.PLATFORM_NAME;
        String marketB = Fcoin.PLATFORM_NAME;
        this.statistic(marketA, marketB, symbol, coinName);
    }

    private void statistic(String marketA, String marketB, String symbol, String coinName) {
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
        try {
            slackService.sendMessage("Statistic Profit", after.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
