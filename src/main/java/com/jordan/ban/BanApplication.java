package com.jordan.ban;

import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.service.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.UnknownHostException;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class BanApplication {

    public static void main(String[] args) {

        try {
            ElasticSearchClient.initClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        ConfigurableApplicationContext context = SpringApplication.run(BanApplication.class, args);
        AccountService accountService = context.getBean(AccountService.class);
//        accountService.emptyAccount();
//        accountService.initAccount();
        ConsumerApplication application = context.getBean(ConsumerApplication.class);
        application.consumer();
        System.out.println("Consumer Started!");
    }

    public static void receiveDiff(ConsumerApplication application, String topic) {
        System.out.println("Topic:" + topic + "-depth");
        application.receiveDepthDiff(topic + "-depth");
    }
}
