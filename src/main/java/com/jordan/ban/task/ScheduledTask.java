package com.jordan.ban.task;

import com.jordan.ban.common.Constant;
import com.jordan.ban.common.Context;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.CycleType;
import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.OrderService;
import com.jordan.ban.service.SlackService;
import com.sun.tools.internal.jxc.ap.Const;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    @Autowired
    private SlackService slackService;


    private static final long CHECK_ORDER_RATE = 2000;//5 second
    private static final long CHECK_ORDER_DELAYT = 10000;//10 second

    @Scheduled(initialDelay = CHECK_ORDER_DELAYT, fixedRate = CHECK_ORDER_RATE)
    public void watchUnfilledOrder() {
        List<Order> list = orderService.getUnfilledOrders();
        if (list != null && list.size() > 0) {
            log.info("Waiting for fix order:[{}]" + list.size());
            Context.setUnFilledOrderNum(list.size());
        }
        list.forEach(order -> {
            this.orderService.refreshOrderState(order);
        });
    }

    /**
     * 每天中午十二点触发
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void statisticProfit() {

        String symbol = "ltcusdt";
        Map<String, BalanceDto> huobiBalance = accountService.findBalancesCache(Huobi.PLATFORM_NAME);
        AccountDto accountA = AccountDto.builder().money(huobiBalance.get("usdt").getBalance()).platform(Huobi.PLATFORM_NAME).symbol(symbol)
                .virtualCurrency(huobiBalance.get("ltc").getBalance()).build();
        Map<String, BalanceDto> fcoinBalance = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        AccountDto accountB = AccountDto.builder().money(fcoinBalance.get("usdt").getBalance()).platform(Fcoin.PLATFORM_NAME).symbol(symbol)
                .virtualCurrency(fcoinBalance.get("ltc").getBalance()).build();

        ProfitStatistics before = profitStatisticsRepository.findTopBySymbolAndAndPlatformAAndAndPlatformBOrderByCreateTimeDesc(symbol, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME);
        double moneyBefore, coinBefore, increase, increasePercent, moneyAfter, coinAfter;
        moneyAfter = accountA.getMoney() + accountB.getMoney();
        coinAfter = accountA.getVirtualCurrency() + accountB.getVirtualCurrency();
        if (before != null) {
            moneyBefore = before.getSumMoney();
            coinBefore = before.getSumCoin();
        } else {
            moneyBefore = moneyAfter;
            coinBefore = coinAfter;
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