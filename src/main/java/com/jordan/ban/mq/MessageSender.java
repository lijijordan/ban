package com.jordan.ban.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MessageSender {

    private ConcurrentHashMap<String, Channel> channels;
    private Connection connection;

    public MessageSender() {
        channels = new ConcurrentHashMap();
        connection = MQConnectionFactory.getConnection();
    }

    public void send(String topic, String message) throws IOException {
        Channel channel = channels.get(topic);
        if (channel == null) {
            channel = connection.createChannel();
            channel.queueDeclare(topic, false, false, false, null);
            channels.put(topic, channel);
        }
        channel.basicPublish("", topic, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Send '" + message + "'");
    }
}
