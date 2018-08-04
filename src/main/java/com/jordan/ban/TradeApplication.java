package com.jordan.ban;

import com.jordan.ban.market.TradeApp;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.service.GridService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.TimeZone;

import static com.jordan.ban.common.Constant.ETH_USDT;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class TradeApplication {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);
        final String symbol = ETH_USDT;

//        final double totalCoin = 2.1022343122678095; // fixme
//        GridService gridService = context.getBean(GridService.class);
//        gridService.initGrid(symbol, 0.005f, 0.01f, 0.1f, totalCoin);
//        gridService.initGrid(symbol, 0.01f, 0.02f, 0.2f, totalCoin);
//        gridService.initGrid(symbol, 0.02f, 0.03f, 0.3f, totalCoin);
//        gridService.initGrid(symbol, 0.03f, 0.04f, 0.2f, totalCoin);
//        gridService.initGrid(symbol, 0.04f, 1f, 0.2f, totalCoin);

        TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff(symbol);

        System.out.println("Listener Started!");
        System.out.println(new Date());
    }


}
