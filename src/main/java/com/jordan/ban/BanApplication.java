package com.jordan.ban;

import com.jordan.ban.entity.Account;
import com.jordan.ban.service.AccountService;
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
public class BanApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BanApplication.class, args);
        AccountService accountService = context.getBean(AccountService.class);
//        accountService.initAccount();

        ConsumerApplication application = new ConsumerApplication();
        receiveDiff(application, "NEOUSDT");
        receiveDiff(application, "EOSUSDT");
        receiveDiff(application, "BTCUSDT");
        receiveDiff(application, "EOSETH");
        receiveDiff(application, "EOSBTC");
        receiveDiff(application, "OMGETH");
        receiveDiff(application, "GXSETH");
        receiveDiff(application, "LTCBTC");
        receiveDiff(application, "BCHUSDT");
        System.out.println("Consumer Started!");
    }

    public static void receiveDiff(ConsumerApplication application, String topic) {
        System.out.println("Topic:" + topic + "-depth");
        application.receiveDepthDiff(topic + "-depth");
    }
}
