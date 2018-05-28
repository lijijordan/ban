package queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ProducerApp {
    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("204.48.17.119");
            factory.setPort(5672);
            factory.setHandshakeTimeout(60 * 1000);
            factory.setUsername("btc");
            factory.setPassword("btc@123");
            factory.setVirtualHost("btc");

            //创建与RabbitMQ服务器的TCP连接  
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare("test", true, false, false, null);
            String message = "First Message";
            channel.basicPublish("", "tes" +
                    "'33", null, message.getBytes());
            System.out.println("Send Message is:'" + message + "'");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}