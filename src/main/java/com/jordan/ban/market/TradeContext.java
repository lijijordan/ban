package com.jordan.ban.market;

import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.TradeDirect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;


@Component
public class TradeContext {

    public final static double DEFAULT_METRICS_MAX = 0.022; // 2.2%
    public final static double METRICS_BACK_PERCENT = 0.85;

    private float avgFloatPercent = 0.1f;

    private double wareHouseDiff = 0.008;
    private float minTradeFloat = 0.025f;

    private double a2bCurrentPercent;
    private double a2bCurrentVolume;

    private double b2aCurrentPercent;
    private double b2aCurrentVolume;

    private double currentEthPrice;

    public double getCurrentEthPrice() {
        return currentEthPrice;
    }
    public void setCurrentEthPrice(double currentEthPrice) {
        this.currentEthPrice = currentEthPrice;
    }
    public double getA2bCurrentVolume() {
        return a2bCurrentVolume;
    }
    public void setA2bCurrentVolume(double a2bCurrentVolume) {
        this.a2bCurrentVolume = a2bCurrentVolume;
    }
    public double getB2aCurrentVolume() {
        return b2aCurrentVolume;
    }
    public void setB2aCurrentVolume(double b2aCurrentVolume) {
        this.b2aCurrentVolume = b2aCurrentVolume;
    }
    public double getA2bCurrentPercent() {
        return a2bCurrentPercent;
    }
    public void setA2bCurrentPercent(double a2bCurrentPercent) {
        this.a2bCurrentPercent = a2bCurrentPercent;
    }
    public double getB2aCurrentPercent() {
        return b2aCurrentPercent;
    }
    public void setB2aCurrentPercent(double b2aCurrentPercent) {
        this.b2aCurrentPercent = b2aCurrentPercent;
    }
    private int orderTryTimes = 10;

    public int getOrderTryTimes() {
        return orderTryTimes;
    }

    public void setOrderTryTimes(int orderTryTimes) {
        this.orderTryTimes = orderTryTimes;
    }

    public float getMinTradeFloat() {
        return minTradeFloat;
    }

    public void setMinTradeFloat(float minTradeFloat) {
        this.minTradeFloat = minTradeFloat;
    }

    public double getWareHouseDiff() {
        return wareHouseDiff;
    }

    public void setWareHouseDiff(double wareHouseDiff) {
        this.wareHouseDiff = wareHouseDiff;
    }

    public float getAvgFloatPercent() {
        return avgFloatPercent;
    }

    public void setAvgFloatPercent(float avgFloatPercent) {
        this.avgFloatPercent = avgFloatPercent;
    }

    private double moveMetrics = DEFAULT_METRICS_MAX;
    private double moveBackMetrics = METRICS_BACK_PERCENT;

    private volatile float upPoint = 0.02f;
    private volatile float downPoint = -0.02f;

    public float getUpPoint() {
        return upPoint;
    }

    public void setUpPoint(float upPoint) {
        this.upPoint = upPoint;
    }

    public float getDownPoint() {
        return downPoint;
    }

    public void setDownPoint(float downPoint) {
        this.downPoint = downPoint;
    }

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
