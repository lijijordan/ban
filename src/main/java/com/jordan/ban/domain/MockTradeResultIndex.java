package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

@Data
public class MockTradeResultIndex {

    /**
     * 价差
     */
    private double eatDiff;
    /**
     * 最小收益率：(A-B/A+B)
     */
    private double eatPercent;

    private double tradeDiff;

    private double tradePercent;

    private TradeDirect tradeDirect;

    private Date createTime;

    private long costTime;

    private String symbol;

    private String diffPlatform;

    //交易数量
    private double eatTradeVolume;
    private double tradeVolume;
}
