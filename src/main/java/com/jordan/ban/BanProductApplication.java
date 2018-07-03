package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.market.parser.*;
import com.jordan.ban.mq.ProductApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.ExecutionException;

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
public class BanProductApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ConfigurableApplicationContext context = SpringApplication.run(BanProductApplication.class, args);
        ProductApplication productApplication = context.getBean(ProductApplication.class);

        // huobi vs dragonex
        productApplication.diffTask(Constant.NEO_USDT, Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.EOS_USDT, Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.BTC_USDT, Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.EOS_ETH, Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, 2000);

        // huobi vs gateio
        productApplication.diffTask(Constant.EOS_BTC, Gateio.PLATFORM_NAME, Huobi.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.EOS_ETH, Gateio.PLATFORM_NAME, Huobi.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.EOS_USDT, Gateio.PLATFORM_NAME, Huobi.PLATFORM_NAME, 2000);

        // fcoin vs huobi
        productApplication.diffTask(Constant.BCH_USDT, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.BTC_USDT, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.ETH_USDT, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.LTC_USDT, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);

        // fcoin vs dragonex
        productApplication.diffTask(Constant.LTC_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.BTC_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.BCH_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 2000);
        productApplication.diffTask(Constant.ETH_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 1000);
        productApplication.diffTask(Constant.XRP_USDT, Dragonex.PLATFORM_NAME, Fcoin.PLATFORM_NAME, 3000);

        System.out.println("Product application Started!");
    }

}
