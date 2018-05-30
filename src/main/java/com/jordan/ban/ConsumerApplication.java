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

    public void receive(String symbol) {
        MessageReceiver receiver = new MessageReceiver((topic, message) -> {
            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.INDEX_NAME);
        });
        try {
            receiver.onReceived(symbol);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsumerApplication application = new ConsumerApplication();
        application.receive("NEOUSDT-differ");
        application.receive("EOSUSDT-differ");
        System.out.println("Consumer Started!");
    }
}