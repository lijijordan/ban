package com.jordan.ban;

import com.jordan.ban.market.TradeApp;
import com.jordan.ban.market.TradeCounter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.TimeZone;

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
        TradeApp tradeApp = context.getBean(TradeApp.class);
        tradeApp.receiveDiff("ETHUSDT");
        TradeCounter.init(6000, 12000);
        System.out.println("Listener Started!");
        System.out.println(new Date());
    }
}
