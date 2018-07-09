package com.jordan.ban.market;

import com.jordan.ban.common.LimitQueue;
import com.jordan.ban.domain.TradeDirect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class TradeCounter {


    public static int QUEUE_SIZE = 6000; // one hour

    LimitQueue<Double> a2bQueue = new LimitQueue<>(QUEUE_SIZE);
    LimitQueue<Double> b2aQueue = new LimitQueue<>(QUEUE_SIZE);

    private double suggestDiffPercent = 0;


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

    private double getMaxDiffPercent(TradeDirect tradeDirect) {
        if (tradeDirect == TradeDirect.A2B) {
            return a2bQueue.stream().mapToDouble(q -> q).max().getAsDouble();
        } else {
            return b2aQueue.stream().mapToDouble(q -> q).max().getAsDouble();
        }
    }

    public double getMaxDiffPercent(boolean direct) {
        double d1 = 0, d2 = 0;
        try{
            d1 = this.getMaxDiffPercent(TradeDirect.A2B);
            d2 = this.getMaxDiffPercent(TradeDirect.B2A);
        }catch (java.util.NoSuchElementException e){
            log.info("queue is not ready!");
        }
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
        this.suggestDiffPercent = result;
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
