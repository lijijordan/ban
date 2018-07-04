package com.jordan.ban.market;

import com.jordan.ban.common.LimitQueue;
import com.jordan.ban.domain.TradeDirect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class TradeCounter {


    private static int QUEUE_SIZE = 60 * 2 * 30 * 2; // 30 min data

    private static LimitQueue<Double> a2bQueue = null;
    private static LimitQueue<Double> b2aQueue = null;

    public static void setQueueSize(int size) {
        log.info("Set queue size:{}", size);
        a2bQueue = new LimitQueue<>(size);
        b2aQueue = new LimitQueue<>(size);
    }

    public long getA2bTradeCount() {
        return a2bQueue.size();
    }


    public long getB2aTradeCount() {
        return b2aQueue.size();
    }

    public void count(TradeDirect direct, double diffPercent) {
        if (direct == TradeDirect.A2B) {
            a2bQueue.offer(diffPercent);
        } else {
            b2aQueue.offer(diffPercent);
        }
    }

    private double sum(LimitQueue<Double> queue) {
        return queue.stream().mapToDouble(q -> q).sum();
    }


    public double getAvgDiffPercent(TradeDirect tradeDirect) {
        if (this.getA2bTradeCount() == 0 || this.getB2aTradeCount() == 0) {
            return 0;
        }
        if (tradeDirect == TradeDirect.A2B) {
            return sum(this.a2bQueue) / this.getA2bTradeCount();
        } else {
            return sum(this.b2aQueue) / this.getB2aTradeCount();
        }
    }

    public double getSuggestDiffPercent() {
        double result = (Math.abs(this.getAvgDiffPercent(TradeDirect.A2B)) +
                Math.abs(this.getAvgDiffPercent(TradeDirect.B2A))) / 2;
        if (result == 0) {
            return TradeContext.DEFAULT_METRICS_MAX;
        }
        return result;
    }

    public static void main(String[] args) {
        LimitQueue<Double> queue = new LimitQueue<>(QUEUE_SIZE);
        queue.offer(1d);
        queue.offer(2d);
        queue.offer(2d);
        System.out.println(queue.stream().mapToDouble(q -> q).sum());
    }
}
