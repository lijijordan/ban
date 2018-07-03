package com.jordan.ban;

import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.service.BackTestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.UnknownHostException;
import java.text.ParseException;


@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class BackTestApplication {

    public static void main(String[] args) throws ParseException, UnknownHostException {

        ConfigurableApplicationContext context = SpringApplication.run(BackTestApplication.class, args);

        BackTestService backTestService = context.getBean(BackTestService.class);
        ElasticSearchClient.initClient();


        System.out.println("Ready to go!");
        backTestService.run();
        System.out.println("Back Test Done!");
    }
}
