package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

/**
 * Real diffMarket for sell and buy list
 */
@Data
public class DifferAskBid {

    private String symbol;
    /**
     * compare result float
     */
    private float differ;
    /**
     * create createTime
     */
    private Date createTime;
    /**
     * compare platform name
     */
    private String differPlatform;

    private TradeType tradeType;

    /**
     * Differ cost time as long
     */
    private long diffCostTime;
}
