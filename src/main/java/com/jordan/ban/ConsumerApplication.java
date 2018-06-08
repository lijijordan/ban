package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.*;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.mq.MessageReceiver;
import lombok.extern.java.Log;
import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;
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

    private AccountDto huobiAccount;
    private AccountDto dragonexAccount;

    private Map<String, DifferAskBid> tradeHistory;

    private String market1LastTradeRecord;
    private String market2LastTradeRecord;

    public void initAccount() {
        // init huobi
        huobiAccount = new AccountDto();
        // USDT
        huobiAccount.setMoney(5000);
        huobiAccount.setVirtualCurrency(100);
        huobiAccount.setPlatform(Huobi.PLATFORM_NAME);

        // init dragonex
        dragonexAccount = new AccountDto();
        // USDT
        dragonexAccount.setMoney(5000);
        dragonexAccount.setVirtualCurrency(100);
        dragonexAccount.setPlatform(Dragonex.PLATFORM_NAME);
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

    public void receiveDepthDiff(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
//            System.out.println(topic + ":" + message);
            // Analysis market diff and direct
            JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
            // TODO: mock trade.

            ElasticSearchClient.indexAsynchronous(jsonObject.getString("a2b"), Constant.MOCK_TRADE_INDEX);
            ElasticSearchClient.indexAsynchronous(jsonObject.getString("b2a"), Constant.MOCK_TRADE_INDEX);
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
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