package com.jordan.ban.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.jordan.ban.market.parser.MarketParser;
import com.jordan.ban.utils.JSONUtil;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * d1_ask:57.16, d1_bid:57.15     d2_ask:56.4918, d2_bid:57.2247
 */
@Data
public class MarketDepth {

    @JsonIgnore
    private Map<Double, Double> orderMap;

    public MarketDepth() {
    }

    public MarketDepth(double d1ask, double d1bid, double d2ask, double d2bid) {
        this.d1ask = d1ask;
        this.d1bid = d1bid;
        this.d2ask = d2ask;
        this.d2bid = d2bid;
    }

    public MarketDepth(double d1ask, double d1askVolume, double d1bid, double d1bidVolume, double d2ask, double d2askVolume, double d2bid, double d2bidVolume) {
        orderMap = new HashMap<>();
        this.d1ask = d1ask;
        this.d1askVolume = d1askVolume;
        this.d1bid = d1bid;
        this.d1bidVolume = d1bidVolume;
        this.d2ask = d2ask;
        this.d2askVolume = d2askVolume;
        this.d2bid = d2bid;
        this.d2bidVolume = d2bidVolume;
        orderMap.put(d1ask, d1askVolume);
        orderMap.put(d2ask, d2askVolume);
        orderMap.put(d1bid, d1bidVolume);
        orderMap.put(d2bid, d2bidVolume);
    }

    public double getVolume(double price) {
        return this.orderMap.get(price);
    }

    private double d1ask;
    private double d1askVolume;
    private double d1bid;
    private double d1bidVolume;

    private double d2ask;
    private double d2askVolume;
    private double d2bid;
    private double d2bidVolume;


    @Override
    public String toString() {
        return JSONUtil.toJsonString(this);
    }
}
