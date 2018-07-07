package com.jordan.ban;

import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.TradeApp;
import com.jordan.ban.mq.ConsumerApplication;
import com.jordan.ban.service.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.UnknownHostException;

import static com.jordan.ban.common.Constant.*;

@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class TacticsApplication {

    public static void main(String[] args) {

        try {
            ElasticSearchClient.initClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ConfigurableApplicationContext context = SpringApplication.run(TacticsApplication.class, args);
        /*AccountService accountService = context.getBean(AccountService.class);
        accountService.emptyAccount();
        accountService.mockAccountTestData();*/
        ConsumerApplication application = context.getBean(ConsumerApplication.class);

//        productApplication.diffTask(Constant.LTC_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
//        productApplication.diffTask(Constant.BTC_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
//        productApplication.diffTask(Constant.BCH_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
//        productApplication.diffTask(Constant.ETH_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 1000);
//        productApplication.diffTask(Constant.XRP_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 3000);
        application.receiveDiff(LTC_USDT);
        application.receiveDiff(BTC_USDT);
        application.receiveDiff(BCH_USDT);
        application.receiveDiff(ETH_USDT);
        application.receiveDiff(XRP_USDT);


        System.out.println("Consumer Started!");

        /*TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff("ETHUSDT");
        System.out.println("Listener Started!");*/
    }

}
