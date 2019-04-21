package com.jordan.ban;

import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.mq.ProductTradeApplication;
import com.jordan.ban.service.GridService;
import com.jordan.ban.service.OrderService;
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
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }


    public static final float PERCENT = 0.1f;
    public static final int SPLIT_COUNT = 100;

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);
        SingleGridService singleGridService = context.getBean(SingleGridService.class);

        // just do it
        singleGridService.generateSingleGrid(SPLIT_COUNT, 172.22, "ethusdt", PERCENT, 6.88f, Fcoin.PLATFORM_NAME);
        System.out.println("------------------ App started ------------------");
    }

}
