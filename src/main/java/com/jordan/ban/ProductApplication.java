package com.jordan.ban;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.DifferAskBid;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.MarketDiffer;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

@Log
public class ProductApplication {

    private final MessageSender sender;

    public ProductApplication() {
        sender = new MessageSender();
    }

    public void send(String topic, String message) throws IOException {
        sender.send(topic, message);
    }

    public static void diffMarket(String symbol, String market1, String market2, long period) {
        String diffTopic = symbol + "-differ";
        ProductApplication productApplication = new ProductApplication();
        MarketDiffer marketDiffer = new MarketDiffer();
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Differ differ = null;
                try {
                    differ = marketDiffer.differ(symbol, market1, market2);
                    if (differ != null) {
                        productApplication.send(diffTopic, JSONUtil.toJsonString(differ));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, period);
    }


    public static void diffAskBid(String symbol) throws ExecutionException, InterruptedException {
        PolicyEngine policyEngine = new PolicyEngine();
        String realDiffTopic = symbol + "-differ-real";
        // 分析买卖盘
        Huobi huobi = new Huobi();
        Dragonex dragonex = new Dragonex();
        ProductApplication productApplication = new ProductApplication();
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                DifferAskBid realDiff = null;
                try {
                    realDiff = policyEngine.analysis(symbol, huobi, dragonex);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                if (realDiff != null) {
//                    log.info(realDiff.toString());
                    try {
                        productApplication.send(realDiffTopic, JSONUtil.toJsonString(realDiff));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, 0, 2000);


    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        String market1 = "Huobi";
        String market2 = "Dragonex";

        // diff market
//        diffMarket("NEOUSDT", market1, market2, 2000);
//        diffMarket("EOSUSDT", market1, market2, 2000);

        diffAskBid("NEOUSDT");
        diffAskBid("EOSUSDT");

    }
}
