package com.jordan.ban;

import com.jordan.ban.market.TradeApp;
import com.jordan.ban.mq.ConsumerApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class TradeApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(TradeApplication.class, args);
        TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff("LTCUSDT");
        System.out.println("Listener Started!");
    }
}
