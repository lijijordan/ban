package com.jordan.ban.market;

import com.jordan.ban.common.Constant;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.mq.MessageReceiver;

import java.io.IOException;

public class ConsumerApplication {

    public static void main(String[] args) throws IOException {

        ElasticSearchClient.initClient();

        MessageReceiver receiver = new MessageReceiver((topic, message) -> {
            System.out.println(String.format("Get message:%s", message));
            ElasticSearchClient.index(message, Constant.INDEX_NAME_SYMBOLS);
        });
        receiver.onReceived(ProductApplication.TOPIC);
        System.out.println("Consumer Started!");
    }
}