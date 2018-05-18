package com.jordan.ban;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ComponentScan("com.jordan.ban")
@SpringBootApplication
public class BanApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BanApplication.class, args);
    }
}
