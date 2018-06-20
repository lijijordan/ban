package com.jordan.ban.market;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.TradeRecord;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.mq.spring.Sender;
import com.jordan.ban.service.MockTradeService;
import com.jordan.ban.service.TradeService;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class TradeApp {


    @Autowired
    private MockTradeService mockTradeService;

    @Autowired
    private Sender sender;

    @Autowired
    private TradeService tradeService;

    public void receiveDepthDiff(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            log.info(topic + ":" + message);
            JSONObject jsonObject = new JSONObject(message);
//            log.info(message);
            this.doDepthDiff(jsonObject.getString("a2b"));
            this.doDepthDiff(jsonObject.getString("b2a"));
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doDepthDiff(String json) {
        MockTradeResultIndex mockTradeResultIndex = JSONUtil.getEntity(json, MockTradeResultIndex.class);
//        this.mockTradeService.mockTrade(mockTradeResultIndex);
        if (mockTradeResultIndex.getDiffPlatform().equals("Huobi-Fcoin") && mockTradeResultIndex.getSymbol().equals("LTCUSDT")) {
            long costTime = System.currentTimeMillis() - mockTradeResultIndex.getCreateTime().getTime();
            if (costTime > 2000) {
                log.info("[{}]second ago,pass it!", costTime / 1000);
                return;
            }
            System.out.println("-------------------------------start trade ---------------------------");
            long start = System.currentTimeMillis();
            try {
                this.tradeService.trade(JSONUtil.getEntity(json, MockTradeResultIndex.class));
            } catch (TradeException e) {
                e.printStackTrace();
            }
            System.out.println("---------------------------------- end ------------------------------- " +
                    (System.currentTimeMillis() - start) + "ms");
        }
    }


    public void receiveDiff(String topic) {
        log.info("Topic:" + topic + "-depth-trade");
        this.receiveDepthDiff(topic + "-depth-trade");
    }
}

