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
import com.jordan.ban.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.jordan.ban.common.Constant.USDT;

@Slf4j
@Component
public class StatisticTask {

    @Autowired
    private StatisticService statisticService;

    /**
     * 每天中午十二点触发
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void statisticDragonexVSHuobiProfit() {
        String symbol = "ethusdt";
        String coinName = "eth";
        String marketA = Dragonex.PLATFORM_NAME;
        String marketB = Fcoin.PLATFORM_NAME;
        this.statisticService.statistic(marketA, marketB, symbol, coinName);
    }


}
