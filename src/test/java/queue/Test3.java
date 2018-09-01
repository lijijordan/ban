package queue;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.utils.JSONUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;

public class Test3 {

    private static int count;

    final BlockingQueue<Integer> blockQueue = new LinkedBlockingDeque<>(10);

    class Producer implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                try {
                    blockQueue.put(1);
                    count++;
                    System.out.println(Thread.currentThread().getName() + " 生产者生产，目前总共有:" + count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Consumer implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    blockQueue.take();
                    count--;
                    System.out.println(Thread.currentThread().getName() + " 消费者消费，目前总共有:" + count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        Test3 test3 = new Test3();
        new Thread(test3.new Producer()).start();
        new Thread(test3.new Consumer()).start();

    }


    @Test
    public void testSchedule() {

        Timer timer1 = new Timer();
        timer1.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("RUN!!!");
                    Thread.sleep(5000);
                    System.out.println("done!");

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, 0, 1000);

        try {
            Thread.sleep(1000 * 200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
