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
//        application.receiveDiff("EOSUSDT");
        application.receiveDiff("BTCUSDT");
//        application.receiveDiff("EOSETH");
//        application.receiveDiff("EOSBTC");
//        application.receiveDiff("OMGETH");
//        application.receiveDiff("GXSETH");
//        application.receiveDiff("LTCBTC");
//        application.receiveDiff("BCHUSDT");
//        application.receiveDiff("ETHUSDT");
//        application.receiveDiff("LTCUSDT");
//        application.receiveDiff("NEOUSDT");

        application.receiveDiff("ETHUSDT");
        System.out.println("Consumer Started!");

        /*TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff("ETHUSDT");
        System.out.println("Listener Started!");*/
    }

}
