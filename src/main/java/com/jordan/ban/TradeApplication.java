package com.jordan.ban;

import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.mq.ProductTradeApplication;
import com.jordan.ban.service.GridService;
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

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);
        GridService gridService = context.getBean(GridService.class);
        ProductTradeApplication productTradeApplication = context.getBean(ProductTradeApplication.class);

        // websocket market trade
        ((Fcoin) MarketFactory.getMarket(Fcoin.PLATFORM_NAME)).connect();
        ((Fcoin) MarketFactory.getMarket(Fcoin.PLATFORM_NAME)).setConnectionLostTimeout(-1);
        ((Dragonex) MarketFactory.getMarket(Dragonex.PLATFORM_NAME)).connect();

        //Rest API market trade
//        productTradeApplication.depthTrade(ETH_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 500);
        System.out.println("------------------ App started ------------------");
    }


    /**
     * ================== init grid ================
     * 2018-08-15 23:24:25.063  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 0.0%~0.5% : 10.0%
     * 2018-08-15 23:24:25.160  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 0.5%~1.0% : 20.0%
     * 2018-08-15 23:24:25.170  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 1.0%~1.5% : 20.0%
     * 2018-08-15 23:24:25.174  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 1.5%~2.0% : 20.0%
     * 2018-08-15 23:24:25.179  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 2.0%~2.5% : 10.0%
     * 2018-08-15 23:24:25.190  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 2.5%~3.0% : 10.0%
     * 2018-08-15 23:24:25.198  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 3.0%~4.0% : 5.0%
     * 2018-08-15 23:24:25.206  INFO 23300 --- [  restartedMain] com.jordan.ban.service.GridService       : Init grid web: 4.0%~100.0% : 5.0%
     *
     * @param gridService
     */
    public static void initETHGrid(GridService gridService) {
        log.info("init grid.....");
        if (gridService.findGridsBySymbol(ETH_USDT).isEmpty()) {
            final double totalCoin = 5.14261775; // todo

            gridService.initGrid(ETH_USDT, -0.005f, 0f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, -0.01f, -0.005f, 0.1f, totalCoin);


            gridService.initGrid(ETH_USDT, 0.01f, 0.015f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.015f, 0.02f, 0.1f, totalCoin);


            // 6

            gridService.initGrid(ETH_USDT, 0f, 0.005f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.005f, 0.01f, 0.2f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.02f, 0.025f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.025f, 0.03f, 0.1f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.03f, 0.04f, 0.05f, totalCoin);
            gridService.initGrid(ETH_USDT, 0.04f, 1f, 0.05f, totalCoin);
        }
    }


    public static void initETHGridPercentOne(GridService gridService, double totalCoin) {
        log.info("init grid....");
        if (gridService.findGridsBySymbol(ETH_USDT).isEmpty()) {
            float f = 0.001f;
            float low = 0, high = 0;
            for (int i = 0; i < 10; i++) {
                low = high;
                high = low + f;
                log.info("low:{},high:{}", low, high);
                gridService.initGrid(ETH_USDT, low, high, 0.1f, totalCoin);
            }
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
