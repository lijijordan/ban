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
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
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
//            log.info(topic + ":" + message);
            // Analysis market diff and direct
            JSONObject jsonObject = new JSONObject(message);
//            log.info(message);
            ElasticSearchClient.indexAsynchronous(jsonObject.getString("a2b"), Constant.MOCK_TRADE_INDEX);
            ElasticSearchClient.indexAsynchronous(jsonObject.getString("b2a"), Constant.MOCK_TRADE_INDEX);

            // TODO: mock trade.
//            log.info(message);
//            tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
//            tradeService.trade(JSONUtil.getEntity(jsonObject.getString("b2a"), MockTradeResultIndex.class));

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

        receiveDiff(application, "ETHUSDT");
        receiveDiff(application, "LTCUSDT");

        log.info("Consumer Started!");
    }

    public static void receiveDiff(ConsumerApplication application, String topic) {
//        application.receiveMarket(topic + "-differ");
        log.info("Topic:" + topic + "-depth");
        application.receiveDepthDiff(topic + "-depth");
    }
}