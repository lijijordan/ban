package com.jordan.ban.market;

import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.TradeDirect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class TradeContext {

    private static double DEFAULT_METRICS_MAX = 0.022; // 2.2%
    private static double METRICS_BACK_PERCENT = 0.85;

    private double moveMetrics = DEFAULT_METRICS_MAX;
    private double moveBackMetrics = METRICS_BACK_PERCENT;

    /**
     * 上次交易的方向
     */
    private TradeDirect lastTradeDirect;




    public TradeDirect getLastTradeDirect() {
        return lastTradeDirect;
    }

    public void setLastTradeDirect(TradeDirect lastTradeDirect) {
        this.lastTradeDirect = lastTradeDirect;
    }


    public double getMoveMetrics() {
        return moveMetrics;
    }

    public void setMoveMetrics(double moveMetrics) {
        this.moveMetrics = moveMetrics;
    }

    public double getMoveBackMetrics() {
        return moveBackMetrics;
    }

    public void setMoveBackMetrics(double moveBackMetrics) {
        this.moveBackMetrics = moveBackMetrics;
    }
}
