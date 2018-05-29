package com.jordan.ban;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.MarketDiffer;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class ProductApplication {

    private final MessageSender sender;

    public ProductApplication() {
        sender = new MessageSender();
    }

    public void send(String topic, String message) throws IOException {
        sender.send(topic, message);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        String symbol = "NEOUSDT";
        String topic = symbol + "-differ";
        String market1 = "Huobi";
        String market2 = "Dragonex";

        // =======
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
                        productApplication.send(topic, JSONUtil.toJsonString(differ));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, 2000);

    }
}
