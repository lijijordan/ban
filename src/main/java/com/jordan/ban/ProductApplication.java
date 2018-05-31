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

    private static final MessageSender sender = new MessageSender();

    public void send(String topic, String message) throws IOException {
        sender.send(topic, message);
    }

    public static void diffMarket(String symbol, String market1, String market2, long period) {
        String diffTopic = symbol + "-differ";
        MarketDiffer marketDiffer = new MarketDiffer();
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Differ differ = null;
                try {
                    differ = marketDiffer.differ(symbol, market1, market2);
                    if (differ != null) {
                        sender.send(diffTopic, JSONUtil.toJsonString(differ));
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
                    log.info(realDiff.toString());
                    try {
                        sender.send(realDiffTopic, JSONUtil.toJsonString(realDiff));
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

        String symbol1 = "NEOUSDT";
        String symbol2 = "EOSUSDT";
        String symbol3 = "BTCUSDT";

        // diff market
        diffTask(symbol1, market1, market2, 2000);
        diffTask(symbol2, market1, market2, 2000);
        diffTask(symbol3, market1, market2, 2000);

    }

    private static void diffTask(String symbol, String market1, String market2, long period) throws ExecutionException, InterruptedException {
        diffMarket(symbol, market1, market2, period);
        diffAskBid(symbol);
    }
}
