package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Account;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.DifferAskBid;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


@Log
public class ConsumerApplication {

    private PolicyEngine policyEngine;

    private int tradeCount = 0;

    private Account huobiAccount;
    private Account dragonexAccount;

    private Map<String, DifferAskBid> tradeHistory;

    public void initAccount() {
        // init huobi
        huobiAccount = new Account();
        huobiAccount.setId(1);
        // USDT
        huobiAccount.setMoney(1200);
        huobiAccount.setVirtualCurrency(100);
        huobiAccount.setPlatform(Huobi.PLATFORM_NAME);
        huobiAccount.setName("huobi");

        // init dragonex
        dragonexAccount = new Account();
        dragonexAccount.setId(2);
        // USDT
        dragonexAccount.setMoney(1200);
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

    public void receive(String topic) {
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
//            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.REAL_DIFF_INDEX);
            // Mock trade event!!
            mockTrade(JSONUtil.getEntity(message, DifferAskBid.class));
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mockTrade(DifferAskBid diff) {
        // NEOUSDT wallet mockGet message:
        if (!diff.getSymbol().equals("EOSUSDT")) {
            return;
        }
        // only one time trade is important
        String key = String.valueOf(diff.getAsk1Price()) + String.valueOf(diff.getAsk1Volume())
                + String.valueOf(diff.getBid1Price()) + String.valueOf(diff.getBid1Volume());
        if (this.tradeHistory.get(key) != null) {
            return;
        } else {
            this.tradeHistory.put(key, diff);
        }
        System.out.println("Check Diff:" + diff.getDiffer());
        if (Math.abs(diff.getDiffer()) < 0.012) {
            return;
        }
        System.out.println("***************************************************************");
        System.out.println("             Ready to mock trade : " + diff.getSymbol());
        printAccount();
        System.out.println(diff.toString());
        float diffValue = diff.getDiffer();
        // init
        double money1 = huobiAccount.getMoney();
        double coin1 = huobiAccount.getVirtualCurrency();
        double money2 = dragonexAccount.getMoney();
        double coin2 = dragonexAccount.getVirtualCurrency();
        double beforeMoney = money1 + money2;
        double minTradeVolume = Math.min(diff.getAsk1Volume(), diff.getBid1Volume());
        // trade
        if (diffValue > 0) { // market1 sell coin
            money1 = money1 + (diff.getAsk1Price() * minTradeVolume)
                    - (diff.getAsk1Price() * minTradeVolume * 0.002);
            coin1 = coin1 - minTradeVolume;
            // Market2

            money2 = money2 - (diff.getBid1Price() * minTradeVolume)
                    - (diff.getBid1Price() * minTradeVolume * 0.002);
            coin2 = coin2 + minTradeVolume;
        } else { // market1 buy coin
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
        System.out.println("Diff Money=" + diffMoney);
        if (diffMoney <= 0) {
            System.out.println("Do Nothing!");
            System.out.println("***************************************************************");
            return;
        }

        if (money1 < 0 || coin1 < 0 || money2 < 0 || coin2 < 0) {
            System.out.println("Not validate trade !");
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
        System.out.println(String.format("\r\nTimes:%s     Trade volume:%s   Before Money:%s  \r\n Total Money:%s  Total Coins:%s \r\n  Diff Money:%s   Diff Money Percent:%s",
                tradeCount, minTradeVolume, beforeMoney, afterMoney, (coin1 + coin2), diffMoney, (diffMoney / beforeMoney) * 100));
        System.out.println("***************************************************************");
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
        application.receive(topic + "-differ");
        application.receiveRealDiff(topic + "-differ-real");

    }
}