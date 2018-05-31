package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.DifferAskBid;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.policy.PolicyEngine;
import com.jordan.ban.mq.MessageReceiver;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;

import java.io.IOException;
import java.net.UnknownHostException;


@Log
public class ConsumerApplication {

    private PolicyEngine policyEngine;

    public ConsumerApplication() {

        policyEngine = new PolicyEngine();
        try {
            ElasticSearchClient.initClient();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void receive(String topic) {
        MessageReceiver receiver = new MessageReceiver((t, message) -> {
            System.out.println(String.format("Get message:%s", message));
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
            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.REAL_DIFF_INDEX);
        });
        try {
            receiver.onReceived(topic);
        } catch (IOException e) {
            e.printStackTrace();
        }
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