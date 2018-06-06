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
}
