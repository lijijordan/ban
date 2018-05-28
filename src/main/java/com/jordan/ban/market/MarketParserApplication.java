package com.jordan.ban.market;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.DragonexParser;
import com.jordan.ban.market.parser.HuobiParser;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;
import org.elasticsearch.client.ElasticsearchClient;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MarketParserApplication {

    public static final String TOPIC = "NEOUSDT";

    private final MessageSender sender;

    public MarketParserApplication() {
        sender = new MessageSender();
    }

    public void send(String message) throws IOException {
        sender.send(TOPIC, message);
    }

    public static void main(String[] args) {
        DragonexParser dragonexParser = new DragonexParser();
        HuobiParser huobiParser = new HuobiParser();
        MarketParserApplication application = new MarketParserApplication();

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Symbol symbol = dragonexParser.parse("NEOUSDT", 129);
                try {
                    application.send(JSONUtil.toJsonString(symbol));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);

        Timer timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                Symbol symbol = huobiParser.parse("neousdt");
                try {
                    application.send(JSONUtil.toJsonString(symbol));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);


        // Differ timer
        /*Timer timer3 = new Timer();
        timer3.schedule(new TimerTask() {
            @Override
            public void run() {
                // differ
                if (huobiSymbol != null && dragonexSymbol != null) {
                    double differ = (huobiSymbol.getPrice() - dragonexSymbol.getPrice())
                            / Math.max(huobiSymbol.getPrice(), dragonexSymbol.getPrice());
                    DecimalFormat df = new DecimalFormat("##.##%");
                    String formattedPercent = df.format(differ);
                    Differ differObject = new Differ();
                    differObject.setSymbol("NEOUSDT");
                    differObject.setPercentDiffer(formattedPercent);
                    differObject.setCreateTime(new Date());
                    differObject.setDiffer((float)differ);
                    differObject.setDifferPlatform("dragonex-huobi");
                    ElasticSearchClient.index(JSONUtil.toJsonString(
                            differObject));
                }
            }
        }, 0, 1000);*/

    }
}
