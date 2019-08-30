package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.service.SingleGridService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
@Slf4j
public class TradeApplication {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }


    public static final float PERCENT = 0.1f;
    public static final int SPLIT_COUNT = 100;

    public static final double CURRENT_PRICE = 167;
    public static final float TOTAL_COIN = 10f;


    public static void main(String[] args) {

        System.out.println("------------------ App starting ------------------");
        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);
        SingleGridService singleGridService = context.getBean(SingleGridService.class);
        WatchAndTrader trader = context.getBean(WatchAndTrader.class);

        //  init
        singleGridService.generateSingleGrid(SPLIT_COUNT, CURRENT_PRICE, "ethusdt", PERCENT, TOTAL_COIN, Fcoin.PLATFORM_NAME);

            // trade
//        trader.watchTrade(Constant.ETH_USDT, Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, 1000, false);

        System.out.println("------------------ App started ------------------");
    }

}
