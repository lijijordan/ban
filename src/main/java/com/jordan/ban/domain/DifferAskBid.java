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

    /**
     * Differ cost time as long
     */
    private long diffCostTime;

    private double ask1Price;
    private double ask1Volume;

    private double bid1Price;
    private double bid1Volume;
}
