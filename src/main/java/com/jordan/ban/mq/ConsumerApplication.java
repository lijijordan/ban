package com.jordan.ban.mq;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.es.ElasticSearchClient;
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
public class ConsumerApplication {


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
        ElasticSearchClient.indexAsynchronous(json, Constant.MOCK_TRADE_INDEX);
//        this.mockTradeService.mockTrade(JSONUtil.getEntity(json, MockTradeResultIndex.class));
    }

    public void receiveDiff(String topic) {
//        application.receiveMarket(topic + "-differ");
        log.info("Topic:" + topic + "-depth");
        this.receiveDepthDiff(topic + "-depth");
    }

    public void sendToES(String topic, String message) {
        sender.send(topic + "-elk", message);
    }
}

