package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.*;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.service.TradeService;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


@Log
@Service
public class ConsumerApplication {

    @Autowired
    private TradeService tradeService;


    public ConsumerApplication() {
        // Mock account
        try {
            ElasticSearchClient.initClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void receiveDepthDiff(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            System.out.println(topic + ":" + message);
            // Analysis market diff and direct
            JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
            ElasticSearchClient.indexAsynchronous(jsonObject.getString("a2b"), Constant.MOCK_TRADE_INDEX);
            ElasticSearchClient.indexAsynchronous(jsonObject.getString("b2a"), Constant.MOCK_TRADE_INDEX);

            // TODO: mock trade.
            log.info(message);
            tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
            tradeService.trade(JSONUtil.getEntity(jsonObject.getString("b2a"), MockTradeResultIndex.class));

        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsumerApplication application = new ConsumerApplication();
        receiveDiff(application, "NEOUSDT");
        receiveDiff(application, "EOSUSDT");
        receiveDiff(application, "BTCUSDT");

        receiveDiff(application, "EOSETH");
        receiveDiff(application, "EOSBTC");
        receiveDiff(application, "OMGETH");
        receiveDiff(application, "GXSETH");
        receiveDiff(application, "LTCBTC");
        receiveDiff(application, "BCHUSDT");
        System.out.println("Consumer Started!");
    }

    public static void receiveDiff(ConsumerApplication application, String topic) {
//        application.receiveMarket(topic + "-differ");
        System.out.println("Topic:" + topic + "-depth");
        application.receiveDepthDiff(topic + "-depth");
    }
}