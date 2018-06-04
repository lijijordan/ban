package com.jordan.ban.domain;

import lombok.Data;

/**
 * d1_ask:57.16, d1_bid:57.15     d2_ask:56.4918, d2_bid:57.2247
 */
@Data
public class MarketDepth {
    public MarketDepth() {
    }

    public MarketDepth(double d1ask, double d1bid, double d2ask, double d2bid) {
        this.d1ask = d1ask;
        this.d1bid = d1bid;
        this.d2ask = d2ask;
        this.d2bid = d2bid;
    }

    private double d1ask;
    private double d1askVolume;
    private double d1bid;
    private double d1bidVolume;

    private double d2ask;
    private double d2askVolume;
    private double d2bid;
    private double d2bidVolume;
}
