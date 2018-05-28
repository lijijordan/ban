package com.jordan.ban.mq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MessageReceiver {

    private final MessageReceiveCallback callback;

    private ConcurrentHashMap<String, Channel> channels;
    private Connection connection;

    public MessageReceiver(MessageReceiveCallback callback) {
        this.callback = callback;
        channels = new ConcurrentHashMap();
        connection = MQConnectionFactory.getConnection();
    }

    public void onReceived(String topic) throws IOException {
        Channel channel = channels.get(topic);
        if (channel == null) {
            channel = connection.createChannel();
            channel.queueDeclare(topic, false, false, false, null);
            channels.put(topic, channel);
        }
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                callback.callback(topic, message);
            }
        };
        channel.basicConsume(topic, true, consumer);
    }

}
