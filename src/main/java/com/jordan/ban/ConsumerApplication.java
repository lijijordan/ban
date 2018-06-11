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
            this.tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
            this.tradeService.trade(JSONUtil.getEntity(jsonObject.getString("b2a"), MockTradeResultIndex.class));

        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void consumer() {
        receiveDiff("EOSUSDT");
        receiveDiff("BTCUSDT");

        receiveDiff("EOSETH");
        receiveDiff("EOSBTC");
        receiveDiff("OMGETH");
        receiveDiff("GXSETH");
        receiveDiff("LTCBTC");
        receiveDiff("BCHUSDT");

        receiveDiff("ETHUSDT");
        receiveDiff("LTCUSDT");
        receiveDiff("NEOUSDT");

        log.info("Consumer Started!");
    }

    public void receiveDiff(String topic) {
//        application.receiveMarket(topic + "-differ");
        log.info("Topic:" + topic + "-depth");
        this.receiveDepthDiff(topic + "-depth");
    }
}

