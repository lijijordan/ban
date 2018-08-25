package com.jordan.ban.market;

import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.mq.spring.Sender;
import com.jordan.ban.service.MockTradeService;
import com.jordan.ban.service.TradeServiceETH;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.jordan.ban.common.Constant.BTC_USDT;
import static com.jordan.ban.common.Constant.ETH_USDT;

@Slf4j
@Service
public class TradeApp {

    private static final String TRADE_TOPIC_SUFFIX = "-depth-trade";

    @Autowired
    private TradeServiceETH tradeServiceETH;

    public void receiveDepthDiff(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            log.debug(topic + ":" + message);
            JSONObject jsonObject = new JSONObject(message);
//            log.debug(message);
            this.doDepthDiff(jsonObject.getString("a2b"));
            this.doDepthDiff(jsonObject.getString("b2a"));
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO： 异步？
    private void doDepthDiff(String json) {
        MockTradeResultIndex mockTradeResultIndex = JSONUtil.getEntity(json, MockTradeResultIndex.class);
        if (mockTradeResultIndex.getDiffPlatform().equals("Dragonex-Fcoin")) {
            long costTime = System.currentTimeMillis() - mockTradeResultIndex.getCreateTime().getTime();
            if (costTime > 5000) {
                log.debug("[{}]second ago,pass it!", costTime / 1000);
                return;
            }
            log.debug("-------------------------------start trade ---------------------------");
            long start = System.currentTimeMillis();

            try {
//                System.out.println(json);
                MockTradeResultIndex tradeResult = JSONUtil.getEntity(json, MockTradeResultIndex.class);
                String symbol = tradeResult.getSymbol();
                // fixme:do not use hard code
                if (symbol.equals(BTC_USDT)) {
//                    this.tradeServiceBTC.preTrade(tradeResult);
                }
                if (symbol.equals(ETH_USDT)) {
                    boolean b = this.tradeServiceETH.preTrade(tradeResult);
                    if (b) {
                        log.debug("trade cost time:[" +
                                (System.currentTimeMillis() - start) + "]ms");
                    }
                }
            } catch (TradeException e) {
                e.printStackTrace();
            }
            log.debug("---------------------------------- end ------------------------------- " +
                    (System.currentTimeMillis() - start) + "ms");
        }
    }


    public void receiveDiff(String symbol) {
        String topic = symbol + TRADE_TOPIC_SUFFIX;
        log.debug("Listening Topic:" + topic);
        this.receiveDepthDiff(topic);
    }


}

