package com.jordan.ban;

import com.jordan.ban.common.Constant;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.mq.MessageReceiver;

import java.io.IOException;

public class ConsumerApplication {

    public static void main(String[] args) throws IOException {

        String symbol = "NEOUSDT-differ";
        ElasticSearchClient.initClient();

        MessageReceiver receiver = new MessageReceiver((topic, message) -> {
            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.INDEX_NAME);
        });
        receiver.onReceived(symbol);
        System.out.println("Consumer Started!");
    }
}