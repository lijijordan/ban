package com.jordan.ban.mq;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.domain.MockTradeResult;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.market.TradeApp;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import com.jordan.ban.market.trade.TradeHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ProductTradeApplication {

    private static ConcurrentHashMap<String, String> DEPTH_ID = new ConcurrentHashMap<>();

    @Autowired
    private TradeApp tradeApp;

    private void getDepthAndTrade(String symbol, String marketName1, String marketName2, long period) {
        MarketParser m1 = MarketFactory.getMarket(marketName1);
        MarketParser m2 = MarketFactory.getMarket(marketName2);
        Timer timer1 = new Timer();
        String depthTopic = symbol + "-depth";
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    long start = System.currentTimeMillis();
                    Depth depth1 = CompletableFuture.supplyAsync(() -> m1.getDepth(symbol)).get();
                    Depth depth2 = CompletableFuture.supplyAsync(() -> m2.getDepth(symbol)).get();
                    double d1ask = depth1.getAsks().get(0).getPrice();
                    double d1askVolume = depth1.getAsks().get(0).getVolume();
                    double d1bid = depth1.getBids().get(0).getPrice();
                    double d1bidVolume = depth1.getBids().get(0).getVolume();
                    double d2ask = depth2.getAsks().get(0).getPrice();
                    double d2askVolume = depth2.getAsks().get(0).getVolume();
                    double d2bid = depth2.getBids().get(0).getPrice();
                    double d2bidVolume = depth2.getBids().get(0).getVolume();
                    MarketDepth marketDepth = new MarketDepth(d1ask, d1askVolume, d1bid, d1bidVolume, d2ask, d2askVolume, d2bid, d2bidVolume);

                    String depthId = marketDepth.toString();
                    // check id
                    if (DEPTH_ID.get(symbol) == null || !DEPTH_ID.get(symbol).equals(depthId)) {
                        MockTradeResultIndex a2b = a2b(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis(), depthId);
                        MockTradeResultIndex b2a = b2a(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis(), depthId);
                        tradeApp.execute(a2b);
                        tradeApp.execute(b2a);
                        // analysis topic
                    }
                    DEPTH_ID.put(symbol, depthId);
                    log.info("Analysis depth and trade. Cost time:[{}]ms.", System.currentTimeMillis() - start);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }


            }
        }, 0, period);
    }

    public MockTradeResultIndex a2b(MarketDepth marketDepth, Depth depth1, Depth depth2, long costTime, long createTime, String id) {
        MockTradeResult eatAB = TradeHelper.eatA2B(marketDepth);
        MockTradeResult tradeAB = TradeHelper.tradeA2B(marketDepth);
        MockTradeResultIndex indexAB = new MockTradeResultIndex();
        indexAB.setEatDiff(eatAB.getTradeDiff());
        indexAB.setEatPercent(eatAB.getTradePercent());
        indexAB.setTradeDiff(tradeAB.getTradeDiff());
        indexAB.setTradePercent(tradeAB.getTradePercent());
        indexAB.setTradeDirect(eatAB.getTradeDirect());
        indexAB.setCostTime(costTime);
        indexAB.setCreateTime(new Date(createTime));
        indexAB.setSymbol(depth1.getSymbol().toUpperCase());
        indexAB.setDiffPlatform(depth1.getPlatform() + "-" + depth2.getPlatform());
        indexAB.setTradeVolume(tradeAB.getMinTradeVolume());
        indexAB.setEatTradeVolume(eatAB.getMinTradeVolume());
        indexAB.setSellCost(eatAB.getSellCost());
        indexAB.setBuyCost(eatAB.getBuyCost());
        indexAB.setBuyPrice(eatAB.getBuyPrice());
        indexAB.setSellPrice(eatAB.getSellPrice());
        indexAB.setId(id);
        return indexAB;
    }

    public MockTradeResultIndex b2a(MarketDepth marketDepth, Depth depth1, Depth depth2, long costTime, long createTime, String id) {
        MockTradeResult eatBA = TradeHelper.eatB2A(marketDepth);
        MockTradeResult tradeBA = TradeHelper.tradeB2A(marketDepth);
        MockTradeResultIndex indexBA = new MockTradeResultIndex();
        indexBA.setEatDiff(eatBA.getTradeDiff());
        indexBA.setEatPercent(eatBA.getTradePercent());
        indexBA.setTradeDiff(tradeBA.getTradeDiff());
        indexBA.setTradePercent(tradeBA.getTradePercent());
        indexBA.setTradeDirect(tradeBA.getTradeDirect());
        indexBA.setCostTime(costTime);
        indexBA.setCreateTime(new Date(createTime));
        indexBA.setSymbol(depth1.getSymbol().toUpperCase());
        indexBA.setDiffPlatform(depth1.getPlatform() + "-" + depth2.getPlatform());
        indexBA.setTradeVolume(tradeBA.getMinTradeVolume());
        indexBA.setEatTradeVolume(eatBA.getMinTradeVolume());
        indexBA.setSellCost(eatBA.getSellCost());
        indexBA.setBuyCost(eatBA.getBuyCost());
        indexBA.setBuyPrice(eatBA.getBuyPrice());
        indexBA.setSellPrice(eatBA.getSellPrice());
        indexBA.setId(id);
        return indexBA;
    }

    public void depthTrade(String symbol, String dragonex, String fcoin, long period) {
//        diffMarket(symbol, market1, market2, period);
        getDepthAndTrade(symbol, dragonex, fcoin, period);
    }
}
