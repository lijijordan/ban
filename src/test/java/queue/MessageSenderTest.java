package queue;

import com.jordan.ban.mq.MessageSender;
import org.junit.Test;

import java.io.IOException;

public class MessageSenderTest {
    @Test
    public void send() throws IOException {
        MessageSender messageSender = new MessageSender();
        for (int i = 1; i <= 10; i++) {
            messageSender.send("test-1", "Hello BTC  " + i);
        }
    }
}
