package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.*;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.market.trade.TradeHelper;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Log
public class ConsumerApplication {

    private PolicyEngine policyEngine;

    private static float TO_RIGHT = 0.012f;
    private static float TO_LEFT = 0.002f;
    // 往回搬系数
    private static float COIN_BACK_VAL = 0.8f;


    private int tradeCount = 0;
    // 平均搬砖利润
    private double avgTradeDiff = 0;

    private Account huobiAccount;
    private Account dragonexAccount;

    private Map<String, DifferAskBid> tradeHistory;

    private String market1LastTradeRecord;
    private String market2LastTradeRecord;

    public void initAccount() {
        // init huobi
        huobiAccount = new Account();
        huobiAccount.setId(1);
        // USDT
        huobiAccount.setMoney(5000);
        huobiAccount.setVirtualCurrency(100);
        huobiAccount.setPlatform(Huobi.PLATFORM_NAME);
        huobiAccount.setName("huobi");

        // init dragonex
        dragonexAccount = new Account();
        dragonexAccount.setId(2);
        // USDT
        dragonexAccount.setMoney(5000);
        dragonexAccount.setVirtualCurrency(100);
        dragonexAccount.setPlatform(Dragonex.PLATFORM_NAME);
        dragonexAccount.setName("dragonex");
        tradeHistory = new HashMap<>();
    }

    public ConsumerApplication() {
        // Mock account
        initAccount();
        policyEngine = new PolicyEngine();
        try {
            ElasticSearchClient.initClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void receiveMarket(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.INDEX_NAME);
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void receiveRealDiff(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
            // Analysis market diff and direct
            ElasticSearchClient.index(message, Constant.REAL_DIFF_INDEX);
            // Mock trade event!!
            mockTrade(JSONUtil.getEntity(message, DifferAskBid.class));
            // TODO：  Valid Trading 模拟买卖
            // 输入：两个A、B市场的买卖盘
            // 策略1：币的流动 ：A->B
            // 策略2：币的流动 ：B->A
//            钱   -> 输出：交易后钱的总量（增加、减少）；
//            币   -> 输出：交易后币量的分布情况（增加、减少），
//            如果 (钱<0 && 币的流向正确)  分析亏损的钱是否符合预期——小于利润的60%
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveDepth(String topic) {


        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            System.out.println(topic + ":" + message);
            // Analysis market diff and direct
            JSONObject jsonObject = new JSONObject(message);
            Depth depth1 = JSONUtil.getEntity(jsonObject.getString("market1_depth"), Depth.class);
            Depth depth2 = JSONUtil.getEntity(jsonObject.getString("market2_depth"), Depth.class);

            long createTime = jsonObject.getLong("createTime");
            long costTime = jsonObject.getLong("costTime");

            double d1ask = depth1.getAsks().get(0).getPrice();
            double d1bid = depth1.getBids().get(0).getPrice();
            double d2ask = depth2.getAsks().get(0).getPrice();
            double d2bid = depth2.getBids().get(0).getPrice();
            MarketDepth marketDepth = new MarketDepth(d1ask, d1bid, d2ask, d2bid);
            index(marketDepth, costTime, createTime, depth1, depth2);
//            tradeIndex(marketDepth, costTime, createTime, depth1, depth2);
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void index(MarketDepth marketDepth, long costTime, long createTime, Depth depth1, Depth depth2) {
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
        ElasticSearchClient.index(JSONUtil.toJsonString(indexAB), Constant.MOCK_TRADE_INDEX);

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
        ElasticSearchClient.index(JSONUtil.toJsonString(indexBA), Constant.MOCK_TRADE_INDEX);
    }


    public void mockTrade(DifferAskBid diff) {
        /*// NEOUSDT wallet mockGet message:
        if (!diff.getSymbol().equals("NEOUSDT")) {
            return;
        }
        // only one time trade is important

        String market1Tick = String.valueOf(diff.getAsk1Price()) + "    " + String.valueOf(diff.getAsk1Volume());
        String market2Tick = String.valueOf(diff.getBid1Price()) + "    " + String.valueOf(diff.getBid1Volume());
        String key = market1Tick + market2Tick;
        if (this.tradeHistory.get(key) != null) {
            return;
        } else {
            this.tradeHistory.put(key, diff);
        }

        if (market1LastTradeRecord != null && this.market1LastTradeRecord.equals(market1Tick)) {
            System.out.println("Trade over:" + market1Tick);
            return;
        }
        if (market2LastTradeRecord != null && this.market2LastTradeRecord.equals(market2Tick)) {
            System.out.println("Trade over:" + market2Tick);
            return;
        }

        System.out.println("Check Diff:" + diff.getDiffer() * 100);
        float diffValue = diff.getDiffer();

        // Judge "Diff Value" and "Direct"
        boolean direct = false;
        direct = (huobiAccount.getVirtualCurrency() - dragonexAccount.getVirtualCurrency()) > 0;
        if (Math.abs(diffValue) < 0.01) {
            return;
        }
        System.out.println("***************************************************************");
        System.out.println("             Ready to mock trade : " + diff.getSymbol());
        printAccount();
        System.out.println(diff.toString());
        // init
        double money1 = huobiAccount.getMoney();
        double coin1 = huobiAccount.getVirtualCurrency();
        double money2 = dragonexAccount.getMoney();
        double coin2 = dragonexAccount.getVirtualCurrency();


        // trade
//        double minTradeVolume = Math.min(diff.getAsk1Volume(), diff.getBid1Volume());
        // FIXME : MOCK 1
        double minTradeVolume = 1;
        if (diffValue > TO_RIGHT) { // market1 sell coin
            money1 = money1 + (diff.getAsk1Price() * minTradeVolume)
                    - (diff.getAsk1Price() * minTradeVolume * 0.002);
            coin1 = coin1 - minTradeVolume;
            // Market2

            money2 = money2 - (diff.getBid1Price() * minTradeVolume)
                    - (diff.getBid1Price() * minTradeVolume * 0.002);
            coin2 = coin2 + minTradeVolume;
        }
        if (diffValue < TO_LEFT) {  // market1 buy coin
            money2 = money2 + (diff.getAsk1Price() * minTradeVolume)
                    - (diff.getAsk1Price() * minTradeVolume * 0.002);
            coin2 = coin2 - minTradeVolume;
            // Market2
            money1 = money1 - (diff.getBid1Price() * minTradeVolume)
                    - (diff.getBid1Price() * minTradeVolume * 0.002);
            coin1 = coin1 + minTradeVolume;
        }

        double afterMoney = money1 + money2;
        double diffMoney = afterMoney - beforeMoney;
        double afterCoinDiffer = Math.abs(coin1 - coin2);
        System.out.println("Diff Money=" + diffMoney);
        System.out.println("Diff Coin=" + afterCoinDiffer);
        // 币量平衡
        boolean coinBalance = afterCoinDiffer < beforeCoinDiffer; // 差值变小了
        // 判断是不是要执行这次交易：需要借助历史数据 来分析是否需要赔钱的交易，平衡两个市场的钱和货币数量，暂时根据经验设置
        if (diffMoney <= 0) { // 赔钱
            if (!coinBalance) {
                System.out.println("Wrong way!Do Nothing!");
                System.out.println("***************************************************************");
                return;
            } else { // 可以平和coin差
                if (Math.abs(diffMoney) > avgTradeDiff * COIN_BACK_VAL) {// 价差大于平均利润的80%,不干了
                    System.out.println(String.format("%s  No money!Do Nothing!", (diffMoney / avgTradeDiff) * 100));
                    System.out.println("***************************************************************");
                    return;
                }
            }
        }

        if (money1 < 0 || coin1 < 0 || money2 < 0 || coin2 < 0) {
            System.out.println("Invalidate : Not enough money or coin.");
            System.out.println("Do Nothing!");
            System.out.println("***************************************************************");
            return;
        }
        huobiAccount.setMoney(money1);
        huobiAccount.setVirtualCurrency(coin1);
        dragonexAccount.setMoney(money2);
        dragonexAccount.setVirtualCurrency(coin2);
        printAccount();
        tradeCount++;
        avgTradeDiff = (avgTradeDiff + diffMoney) / tradeCount;
        this.market1LastTradeRecord = market1Tick;
        this.market2LastTradeRecord = market2Tick;
        System.out.println(String.format("\r\nTimes:%s     Trade volume:%s   Before Money:%s  \r\n Total Money:%s  Total Coins:%s \r\n  Diff Money:%s   Diff Money Percent:%s",
                tradeCount, minTradeVolume, beforeMoney, afterMoney, (coin1 + coin2), diffMoney, (diffMoney / beforeMoney) * 100));
        System.out.println("***************************************************************");*/
    }

    public void printAccount() {
        System.out.println(String.format("Huobi  money:%s, coin:%s",
                String.valueOf(this.huobiAccount.getMoney()), String.valueOf(this.huobiAccount.getVirtualCurrency())));
        System.out.println(String.format("Dragonex  money:%s, coin:%s",
                String.valueOf(this.dragonexAccount.getMoney()), String.valueOf(this.dragonexAccount.getVirtualCurrency())));
        System.out.println(String.format("Total Money:%s   Total Coin:%s",
                (huobiAccount.getMoney() + dragonexAccount.getMoney()), (huobiAccount.getVirtualCurrency() + dragonexAccount.getVirtualCurrency())));
    }

    public static void main(String[] args) {
        ConsumerApplication application = new ConsumerApplication();
        receiveDiff(application, "NEOUSDT");
        receiveDiff(application, "EOSUSDT");
        receiveDiff(application, "BTCUSDT");
        System.out.println("Consumer Started!");
    }

    public static void receiveDiff(ConsumerApplication application, String topic) {
        application.receiveMarket(topic + "-differ");
        application.receiveDepth(topic + "-depth");
    }
}