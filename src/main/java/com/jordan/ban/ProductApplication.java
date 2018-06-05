package com.jordan.ban;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.market.parser.*;
import com.jordan.ban.market.policy.MarketDiffer;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

@Log
public class ProductApplication {

    private static final MessageSender sender = new MessageSender();

    public static void diffMarket(String symbol, String market1, String market2, long period) {
        String diffTopic = symbol + "-differ";
        MarketDiffer marketDiffer = new MarketDiffer();
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Differ differ;
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


    public static void getDepth(String symbol, String marketName1, String marketName2) {
        String depthTopic = symbol + "-depth";
        // 分析买卖盘
        MarketParser m1 = MarketFactory.getMarket(marketName1);
        MarketParser m2 = MarketFactory.getMarket(marketName2);
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String, Object> depthMap = new HashMap<>();
                try {
                    // FIXME: use asynchronous
                    long start = System.currentTimeMillis();
                    Depth depth1 = m1.getDepth(symbol);
                    Depth depth2 = m2.getDepth(symbol);
                    depthMap.put(m1.getName(), depth1);
                    depthMap.put(m2.getName(), depth2);
                    depthMap.put("costTime", (System.currentTimeMillis() - start));
                    depthMap.put("createTime", System.currentTimeMillis());
                    sender.send(depthTopic, JSONUtil.toJsonString(depthMap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000);


    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        String huobi = "Huobi";
        String dragonex = "Dragonex";
        String okex = "Okex";

        String neousdt = "NEOUSDT";
        String eosusdt = "EOSUSDT";
        String btcusdt = "BTCUSDT";

        // diff market
        diffTask(neousdt, huobi, dragonex, 2000);
        diffTask(eosusdt, huobi, dragonex, 2000);
        diffTask(btcusdt, huobi, dragonex, 2000);

        diffTask(btcusdt, huobi, okex, 2000);
        diffTask(eosusdt, huobi, okex, 2000);
        diffTask(neousdt, huobi, okex, 2000);
    }

    private static void diffTask(String symbol, String market1, String market2, long period) throws ExecutionException, InterruptedException {
        diffMarket(symbol, market1, market2, period);
        getDepth(symbol, market1, market2);
    }
}
