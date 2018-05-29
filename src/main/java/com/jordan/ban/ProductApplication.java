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
        ProductApplication productApplication = new ProductApplication();
        MarketDiffer marketDiffer = new MarketDiffer();
        Differ differ = marketDiffer.differ(symbol, "Huobi", "Dragonex");
        if (differ != null) {
            productApplication.send(topic, JSONUtil.toJsonString(differ));
        }

    }
}
