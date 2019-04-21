package com.jordan.ban.task;

import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
        log.info("schedule statistic....");
        this.statisticService.singleGridStatistic();
    }


}
