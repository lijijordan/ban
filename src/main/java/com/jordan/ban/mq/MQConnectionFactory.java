package com.jordan.ban.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

//FIXME : Use spring
public class MQConnectionFactory {

    private static ConnectionFactory factory;
    private static Connection connection;

    private static void initFactory() {
        factory = new ConnectionFactory();
        factory.setHost("204.48.17.119");
        factory.setPort(5672);
        factory.setUsername("btc");
        factory.setPassword("btc@123");
        factory.setVirtualHost("/");
    }


    public static Connection getConnection() {
        if (factory == null) {
            initFactory();
        }
        if (connection != null) {
            return connection;
        } else {
            try {
                connection = factory.newConnection();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            return connection;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
