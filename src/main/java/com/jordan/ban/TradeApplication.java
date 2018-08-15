package com.jordan.ban;

import com.jordan.ban.market.TradeApp;
import com.jordan.ban.service.GridService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import static com.jordan.ban.common.Constant.BTC_USDT;
import static com.jordan.ban.common.Constant.ETH_USDT;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
@Slf4j
public class TradeApplication {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);

        GridService gridService = context.getBean(GridService.class);
        initETHGrid(gridService);
//        initBTCGrid(gridService);

        TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff(ETH_USDT);
        tradeApp.receiveDiff(BTC_USDT);

        System.out.println("Listener Started!");
        System.out.println(new Date());
    }


    /**
     * ================== init grid ================
    5 23:00:49.070  INFO 22638 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 4.0%~100.0% : 5.0%
     *
     * @param gridService
     */
    public static void initETHGrid(GridService gridService) {
        log.info("================== init grid ================");
        if (gridService.findGridsBySymbol(ETH_USDT).isEmpty()) {
            final double totalCoin = 5.14261775; // todo
            gridService.initGrid(ETH_USDT, 0f, 0.005f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.005f, 0.01f, 0.2f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.01f, 0.015f, 0.2f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.015f, 0.02f, 0.2f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.02f, 0.025f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.025f, 0.03f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.03f, 0.04f, 0.05f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.04f, 1f, 0.05f, totalCoin);
        }
    }

    public static void initBTCGrid(GridService gridService) {
        if (gridService.findGridsBySymbol(BTC_USDT).isEmpty()) {
            final double totalCoin = 1000f * 2 / 7000; // fixme
            gridService.initGrid(BTC_USDT, 0.005f, 0.01f, 0.1f, totalCoin);
            gridService.initGrid(BTC_USDT, 0.01f, 0.02f, 0.2f, totalCoin);
            gridService.initGrid(BTC_USDT, 0.02f, 0.03f, 0.3f, totalCoin);
            gridService.initGrid(BTC_USDT, 0.03f, 0.04f, 0.2f, totalCoin);
            gridService.initGrid(BTC_USDT, 0.04f, 1f, 0.2f, totalCoin);
        }
    }
}
