package com.jordan.ban.market;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.Ticker;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.mq.ProductTradeApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.jordan.ban.common.Constant.ETH;
import static com.jordan.ban.common.Constant.ETH_USDT;

@Service
@Slf4j
public class DepthHelper {

    private Depth dragonexDepth;
    private Depth fcoinDepth;

    private String comparedID;

    @Autowired
    private TradeApp tradeApp;

    private static final String symbol = ETH_USDT;
    private static final String TOPIC = "ETHUSDT-depth";
    @Autowired
    private ProductTradeApplication productTradeApplication;

    private List<Ticker> fcoinBuyList;

    private List<Ticker> fcoinSellList;

    private List<Ticker> dragonexBuyList;

    private List<Ticker> dragonexSellList;

    public void setDragonexDepth(Depth dragonexDepth) {
        this.dragonexDepth = dragonexDepth;
        this.trade();
    }

    public void setFcoinDepth(Depth fcoinDepth) {
        this.fcoinDepth = fcoinDepth;
        this.trade();
    }

    // ask=sell bid=buy
    @PostConstruct
    public void init() {
        this.dragonexDepth = new Depth();
        this.dragonexDepth.setPlatform(Dragonex.PLATFORM_NAME);
        this.dragonexDepth.setSymbol(ETH_USDT);

        this.fcoinDepth = new Depth();
        this.fcoinDepth.setPlatform(Fcoin.PLATFORM_NAME);
        this.fcoinDepth.setSymbol(ETH_USDT);
    }

    public void setFcoinBuy(List<Ticker> tickerList) {
        this.fcoinBuyList = tickerList;
        this.fcoinDepth.setBids(tickerList);
        this.trade();
    }

    public void setFcoinSell(List<Ticker> tickerList) {
        this.fcoinSellList = tickerList;
        this.fcoinDepth.setAsks(tickerList);
        this.trade();
    }

    public void setDragonexBuy(List<Ticker> tickerList) {
        this.dragonexBuyList = tickerList;
        this.dragonexDepth.setBids(tickerList);
        this.trade();
    }

    public void setDragonexSell(List<Ticker> tickerList) {
        this.dragonexSellList = tickerList;
        this.dragonexDepth.setAsks(tickerList);
        this.trade();
    }

    private void trade() {
        long start = System.currentTimeMillis();
        Depth depth1 = this.dragonexDepth;
        Depth depth2 = this.fcoinDepth;
        // Fixme
        if (depth1.getAsks() == null || depth1.getBids() == null || depth2.getAsks() == null || depth2.getBids() == null) {
            log.info("Depth Data not ready!!!!");
            return;
        }
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
        if (this.comparedID == null || !this.comparedID.equals(depthId)) {
            MockTradeResultIndex a2b = productTradeApplication.a2b(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis(), depthId);
            MockTradeResultIndex b2a = productTradeApplication.b2a(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis(), depthId);
//            log.info("a2b:{}", a2b.toString());
//            log.info("b2a:{}", b2a.toString());

            tradeApp.execute(a2b);
            tradeApp.execute(b2a);
            // analysis topic
            productTradeApplication.send(TOPIC, a2b, b2a);
        }
        this.comparedID = depthId;
//        log.info("Analysis depth and trade. Cost time:[{}]ms.", System.currentTimeMillis() - start);
    }


}
