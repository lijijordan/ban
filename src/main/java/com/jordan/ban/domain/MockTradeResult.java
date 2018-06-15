package com.jordan.ban.domain;

import lombok.Data;

@Data
public class MockTradeResult {
    public MockTradeResult(double tradeDiff, double tradePercent, TradeDirect tradeDirect, double minTradeVolume) {
        this.tradeDiff = tradeDiff;
        this.tradePercent = tradePercent;
        this.tradeDirect = tradeDirect;
        this.minTradeVolume = minTradeVolume;
    }

    public MockTradeResult(double tradeDiff, double tradePercent, TradeDirect tradeDirect, double minTradeVolume, double sellCost, double buyCost) {
        this.tradeDiff = tradeDiff;
        this.tradePercent = tradePercent;
        this.tradeDirect = tradeDirect;
        this.minTradeVolume = minTradeVolume;
        this.sellCost = sellCost;
        this.buyCost = buyCost;
    }

    public MockTradeResult(double tradeDiff, double tradePercent, TradeDirect tradeDirect, double minTradeVolume, double sellCost, double buyCost, double sellPrice, double buyPrice) {
        this.tradeDiff = tradeDiff;
        this.tradePercent = tradePercent;
        this.tradeDirect = tradeDirect;
        this.minTradeVolume = minTradeVolume;
        this.sellCost = sellCost;
        this.buyCost = buyCost;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
    }

    /**
     * 价差
     */
    private double tradeDiff;
    /**
     * 最小收益率：(A-B/A+B)
     */
    private double tradePercent;

    private TradeDirect tradeDirect;

    /**
     * 最小交易量
     */
    private double minTradeVolume;

    private double sellCost;

    private double buyCost;

    private double sellPrice;

    private double buyPrice;
}
