package queue;

import com.jordan.ban.ProductApplication;
import com.jordan.ban.mq.MessageReceiveCallback;
import com.jordan.ban.mq.MessageReceiver;

import java.io.IOException;

public class MessageReceiverTest {

    public void testReceive() throws IOException {
        MessageReceiver receiver = new MessageReceiver(new MessageReceiveCallback() {
            @Override
            public void callback(String topic, String message) {
                System.out.println(String.format("Get message:%s", message));
            }
        });
        receiver.onReceived("topic");
        try {
            Thread.sleep(1000 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        MessageReceiver receiver = new MessageReceiver((topic, message) -> System.out.println(String.format("Get message:%s", message)));
        receiver.onReceived("topic");
    }
}
