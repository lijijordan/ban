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

    private static LimitQueue<Double> a2bAvgQueue = null;
    private static LimitQueue<Double> b2aAvgQueue = null;

    private static int queueSize;

    private static int avgQueueSize;

    public static void init(int size1, int size2) {
        queueSize = size1;
        avgQueueSize = size2;
        log.info("Set queue size:{}", queueSize);

        a2bQueue = new LimitQueue<>(queueSize);
        b2aQueue = new LimitQueue<>(queueSize);

        a2bAvgQueue = new LimitQueue<>(avgQueueSize);
        b2aAvgQueue = new LimitQueue<>(avgQueueSize);
    }


    public long getA2bTradeCount() {
        return a2bQueue.size();
    }

    public long getA2bAvgTradeCount() {
        return a2bAvgQueue.size();
    }

    public boolean isFull() {
        return a2bQueue.size() >= queueSize;
    }

    public boolean isAvgFull() {
        return a2bAvgQueue.size() >= avgQueueSize;
    }

    public int getSize() {
        return a2bQueue.size();
    }


    public long getB2aTradeCount() {
        return b2aQueue.size();
    }

    public void count(TradeDirect direct, double diffPercent) {
        if (direct == TradeDirect.A2B) {
            a2bQueue.offer(diffPercent);
            a2bAvgQueue.offer(diffPercent);
        } else {
            b2aQueue.offer(diffPercent);
            b2aAvgQueue.offer(diffPercent);
        }
    }


    private double sum(LimitQueue<Double> queue) {
        return queue.stream().mapToDouble(q -> q).sum();
    }


    public double getAvgDiffPercent(TradeDirect tradeDirect) {
        if (this.getA2bAvgTradeCount() == 0 || this.getA2bAvgTradeCount() == 0) {
            return 0;
        }
        if (tradeDirect == TradeDirect.A2B) {
            return sum(this.a2bAvgQueue) / this.getA2bAvgTradeCount();
        } else {
            return sum(this.b2aAvgQueue) / this.getA2bAvgTradeCount();
        }
    }

    public double getMaxDiffPercent(TradeDirect tradeDirect) {
        if (tradeDirect == TradeDirect.A2B) {
            return a2bQueue.stream().mapToDouble(q -> q).max().getAsDouble();
        } else {
            return b2aQueue.stream().mapToDouble(q -> q).max().getAsDouble();
        }
    }

    public double getMaxDiffPercent(boolean direct) {
        double d1 = this.getMaxDiffPercent(TradeDirect.A2B);
        double d2 = this.getMaxDiffPercent(TradeDirect.B2A);
        return parse(direct, d1, d2);
    }

    private double parse(boolean direct, double d1, double d2) {
        if (direct) {
            if (d1 > 0) {
                return d1;
            } else {
                return d2;
            }
        } else {
            if (d1 < 0) {
                return d1;
            } else {
                return d2;
            }
        }
    }


    /**
     * @param direct >0 or <0
     * @return
     */
    public double getAvgDiffPercent(boolean direct) {
        double d1 = this.getAvgDiffPercent(TradeDirect.A2B);
        double d2 = this.getAvgDiffPercent(TradeDirect.B2A);
        return parse(direct, d1, d2);
    }

    public double getSuggestDiffPercent() {
        double result = (Math.abs(this.getAvgDiffPercent(TradeDirect.A2B)) +
                Math.abs(this.getAvgDiffPercent(TradeDirect.B2A))) / 2;
        if (result == 0) {
            return TradeContext.DEFAULT_METRICS_MAX;
        }
        return result;
    }

    public double getSuggestDiffPercent(TradeDirect direct) {
        if (!isAvgFull()) {
            log.info("Avg queue is not ready now.");
            return 0;
        }
        double avgA2B = this.getAvgDiffPercent(TradeDirect.A2B);
        double avgB2A = this.getAvgDiffPercent(TradeDirect.B2A);

        double avg = (Math.abs(avgA2B) + Math.abs(avgB2A)) / 2;

        if (direct == TradeDirect.A2B) {
            return avg * -1;
        } else {
            return avg;
        }
    }

}
