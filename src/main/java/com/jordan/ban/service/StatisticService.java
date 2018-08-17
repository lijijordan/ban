package com.jordan.ban.service;

import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.domain.StatisticRecordDto;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.TradeCounter;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import static com.jordan.ban.common.Constant.ETH;
import static com.jordan.ban.common.Constant.EXCHANGE_RATE;
import static com.jordan.ban.common.Constant.USDT;

@Service
public class StatisticService {


    @Autowired
    private AccountService accountService;

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;


    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private TradeContext tradeContext;

    @Autowired
    private SlackService slackService;

    public void statistic(String marketA, String marketB, String symbol, String coinName) {
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(marketA);
        AccountDto accountA = AccountDto.builder().money(balanceA.get(USDT.toLowerCase()).getAvailable()).platform(marketA).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getAvailable() : 0).build();
        Map<String, BalanceDto> balanceB = accountService.findBalancesCache(marketB);
        AccountDto accountB = AccountDto.builder().money(balanceB.get(USDT.toLowerCase()).getAvailable()).platform(marketB).symbol(symbol)
                .virtualCurrency(balanceB.get(coinName) != null ? balanceB.get(coinName).getAvailable() : 0).build();

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
        // Last 24 hours
        Date date = new Date(System.currentTimeMillis() - 3600 * 24 * 1000);
        StatisticRecordDto statisticRecordDto = tradeRecordService.queryAndStatisticTradeRecord(date);
        if (statisticRecordDto == null) {
            statisticRecordDto = StatisticRecordDto.builder().build();
        }


        // static CNY
        BalanceDto aUsdt = balanceA.get(USDT.toLowerCase());
        BalanceDto bUsdt = balanceB.get(USDT.toLowerCase());
        BalanceDto aEth = balanceA.get(ETH.toLowerCase());
        BalanceDto bEth = balanceB.get(ETH.toLowerCase());

        double totalUsdt = aUsdt.getBalance() + bUsdt.getBalance()
                + ((aEth.getBalance() + bEth.getBalance()) * tradeContext.getCurrentEthPrice());


        ProfitStatistics after = ProfitStatistics.builder().cycleType(CycleType.day).symbol(symbol)
                .increase(increase).sumCoin(coinAfter).sumMoney(moneyAfter)
                .increasePercent(increasePercent).platformA(accountA.getPlatform())
                .sumCostMoney(statisticRecordDto.getSumCostMoney())
                .sumProfit(statisticRecordDto.getSumProfit())
                .avgA2BDiffPercent(statisticRecordDto.getAvgA2BDiffPercent())
                .avgB2ADiffPercent(statisticRecordDto.getAvgB2ADiffPercent())
                .sumA2BProfit(statisticRecordDto.getSumA2BProfit())
                .sumB2AProfit(statisticRecordDto.getSumB2AProfit())
                .avgA2BProfit(statisticRecordDto.getAvgA2BProfit())
                .avgB2AProfit(statisticRecordDto.getAvgB2AProfit())
                .totalUSDT(totalUsdt).totalCNY(EXCHANGE_RATE * totalUsdt)
                .platformB(accountB.getPlatform()).build();
        this.profitStatisticsRepository.save(after);
        slackService.sendMessage("Statistic Profit", after.toString());
    }
}
