package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

@Data
public class MockTradeResultIndex {

    /**
     * 价差
     */
    private double tradeDiff;
    /**
     * 最小收益率：(A-B/A+B)
     */
    private double tradePercent;

    private TradeDirect tradeDirect;

    private Date createTime;

    private long costTime;

    private String symbol;

    private String diffPlatform;
}
