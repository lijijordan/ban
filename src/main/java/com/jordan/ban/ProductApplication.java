package com.jordan.ban;

import com.jordan.ban.domain.*;
import com.jordan.ban.market.parser.*;
import com.jordan.ban.market.policy.MarketDiffer;
import com.jordan.ban.market.trade.TradeHelper;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Log
public class ProductApplication {

    private static final MessageSender sender = new MessageSender();

    public static void diffMarket(String symbol, String market1, String market2, long period) {
        String diffTopic = symbol + "-differ";
        MarketDiffer marketDiffer = new MarketDiffer();
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Differ differ;
                try {
                    differ = marketDiffer.differ(symbol, market1, market2);
                    if (differ != null) {
                        sender.send(diffTopic, JSONUtil.toJsonString(differ));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, period);
    }


    public static void getDepth(String symbol, String marketName1, String marketName2) {
        String depthTopic = symbol + "-depth";
        // 分析买卖盘
        MarketParser m1 = MarketFactory.getMarket(marketName1);
        MarketParser m2 = MarketFactory.getMarket(marketName2);
        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String, Object> mockTrade = new HashMap<>();
                try {
                    // FIXME: use asynchronous
                    long start = System.currentTimeMillis();
                    Depth depth1 = m1.getDepth(symbol);
                    Depth depth2 = m2.getDepth(symbol);
                    double d1ask = depth1.getAsks().get(0).getPrice();
                    double d1askVolume = depth1.getAsks().get(0).getVolume();
                    double d1bid = depth1.getBids().get(0).getPrice();
                    double d1bidVolume = depth1.getBids().get(0).getVolume();
                    double d2ask = depth2.getAsks().get(0).getPrice();
                    double d2askVolume = depth2.getAsks().get(0).getVolume();
                    double d2bid = depth2.getBids().get(0).getPrice();
                    double d2bidVolume = depth2.getBids().get(0).getVolume();
                    MarketDepth marketDepth = new MarketDepth(d1ask, d1askVolume, d1bid, d1bidVolume, d2ask, d2askVolume, d2bid, d2bidVolume);
                    mockTrade.put("a2b", a2b(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));
                    mockTrade.put("b2a", b2a(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));
                    sender.send(depthTopic, JSONUtil.toJsonString(mockTrade));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000);
    }

    private static MockTradeResultIndex a2b(MarketDepth marketDepth, Depth depth1, Depth depth2, long costTime, long createTime) {
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
        return indexAB;
    }

    private static MockTradeResultIndex b2a(MarketDepth marketDepth, Depth depth1, Depth depth2, long costTime, long createTime) {
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
        return indexBA;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        String huobi = "Huobi";
        String dragonex = "Dragonex";
        String okex = "Okex";
        String gateio = "Gateio";
        String bitz = "BitZ";
        String exmo = "Exmo";


        String neousdt = "NEOUSDT";
        String eosusdt = "EOSUSDT";
        String btcusdt = "BTCUSDT";

        String eosbtc = "EOSBTC";
        String eoseth = "EOSETH";
        String omgeth = "OMGETH";
        // 高风险：公信宝
        String gxseth = "GXSETH";
        String ltcbtc = "LTCBTC";
        String bchusdt = "BCHUSDT";

        diffTask(bchusdt, dragonex, exmo, 2000);


        /*// huobi vs dragonex
        diffTask(neousdt, huobi, dragonex, 2000);
        diffTask(eosusdt, huobi, dragonex, 2000);
        diffTask(btcusdt, huobi, dragonex, 2000);
        diffTask(eoseth, huobi, dragonex, 2000);

        // huobi vs okex
        diffTask(btcusdt, huobi, okex, 2000);
        diffTask(eosusdt, huobi, okex, 2000);
        diffTask(neousdt, huobi, okex, 2000);
        diffTask(eosbtc, huobi, okex, 2000);
        diffTask(eoseth, huobi, okex, 2000);
        diffTask(omgeth, huobi, okex, 2000);

        // huobi vs gateio
        diffTask(eosbtc, gateio, huobi, 2000);
        diffTask(eoseth, gateio, huobi, 2000);
        diffTask(eosusdt, gateio, huobi, 2000);
        // bit-z vs gateio
        diffTask(eosbtc, gateio, bitz, 2000);
//        diffTask(ltcbtc, gateio, bitz, 2000);

        // bit-z vs dragonex
//        diffTask(gxseth, dragonex, bitz, 2000);

        // exmo vs drgonex
        diffTask(bchusdt, dragonex, exmo, 2000);
        diffTask(eosusdt, dragonex, exmo, 2000);
*/

    }


    /**
     * Watch a market
     *
     * @param symbolName
     * @param marketName
     * @param period
     */
    private static void watchMarket(String symbolName, String marketName, long period) {
        Timer timer1 = new Timer();
        MarketParser market = MarketFactory.getMarket(marketName);
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                Differ differ;
                try {
                    Symbol symbol = market.getPrice(symbolName);
                    if (symbol != null) {
                        sender.send(symbolName, JSONUtil.toJsonString(symbol));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, 0, period);
    }

    private static void diffTask(String symbol, String market1, String market2, long period) throws ExecutionException, InterruptedException {
//        diffMarket(symbol, market1, market2, period);
        getDepth(symbol, market1, market2);
    }
}
