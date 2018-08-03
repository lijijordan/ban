package com.jordan.ban;

import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.service.BackTestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jordan.ban.common.Constant.BTC_USDT;


@ComponentScan("com.jordan.ban")
@EnableJpaRepositories("com.jordan.ban.dao")
@SpringBootApplication
public class BackTestApplication {

    private static final String ES_HOST = "localhost";

    public static void main(String[] args) throws ParseException, UnknownHostException {

        ConfigurableApplicationContext context = SpringApplication.run(BackTestApplication.class, args);

        ElasticSearchClient.initClient(ES_HOST);

        BackTestService backTestService = context.getBean(BackTestService.class);

        String start = "2018/07/25 23:00:00";
        String end = "2018/07/31 23:00:00";

        System.out.println("------- Ready to go! -------");
//        run(start, end, 12000, ETH_USDT);
//        run(start, end, 6000, LTC_USDT);
//        run(start, end, 6000, BCH_USDT);
//        run(start, end, 6000, ETH_USDT);
        backTestService.run(start, end, 6000, BTC_USDT);
//        run(start, end, 6000, XRP_USDT);
        System.out.println("End at:" + new Date());
        System.out.println("Back Test Done!");
    }
}
