package com.jordan.ban.market;

import com.jordan.ban.domain.TradeDirect;
import org.springframework.stereotype.Component;

@Component
public class TradeCounter {

    private long a2bTradeCount = 0;
    private long b2aTradeCount = 0;
    private double a2bSumDiffPercent = 0;
    private double b2aSumDiffPercent = 0;

    public void count(TradeDirect direct, double diffPercent) {
        if (direct == TradeDirect.A2B) {
            a2bTradeCount++;
            a2bSumDiffPercent = a2bSumDiffPercent + diffPercent;
        } else {
            b2aTradeCount++;
            b2aSumDiffPercent = b2aSumDiffPercent + diffPercent;
        }
    }

    public double getAvgDiffPercent(TradeDirect tradeDirect) {
        if (a2bTradeCount == 0 || b2aTradeCount == 0) {
            return 0;
        }
        if (tradeDirect == TradeDirect.A2B) {
            return a2bSumDiffPercent / a2bTradeCount;
        } else {
            return b2aSumDiffPercent / b2aTradeCount;
        }
    }

}
