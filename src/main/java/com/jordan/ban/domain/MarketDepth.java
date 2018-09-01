package com.jordan.ban.domain;

import com.jordan.ban.utils.JSONUtil;
import lombok.Builder;
import lombok.Data;
import org.apache.logging.log4j.core.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * d1_ask:57.16, d1_bid:57.15     d2_ask:56.4918, d2_bid:57.2247
 */
@Data
public class MarketDepth {

    private Map<Double, Double> orderMap;

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


    public MarketDepth(double d1ask, double d1askVolume, double d1bid, double d1bidVolume, double d2ask, double d2askVolume, double d2bid, double d2bidVolume) {
        this.d1ask = d1ask;
        this.d1askVolume = d1askVolume;
        this.d1bid = d1bid;
        this.d1bidVolume = d1bidVolume;
        this.d2ask = d2ask;
        this.d2askVolume = d2askVolume;
        this.d2bid = d2bid;
        this.d2bidVolume = d2bidVolume;
    }
    public static MarketDepth parse(String json) {
        return JSONUtil.getEntity(json, MarketDepth.class);
    }

    /**
     * All not equals
     *
     * @param o
     * @return
     */
    public boolean notEqualsAll(Object o) {
        if (o == null) {
            return true;
        }
        MarketDepth that = (MarketDepth) o;
        return (
//               Double.compare(that.d1ask, d1ask) != 0 &&
                Double.compare(that.d1askVolume, d1askVolume) != 0 &&
//                Double.compare(that.d1bid, d1bid) != 0 &&
                        Double.compare(that.d1bidVolume, d1bidVolume) != 0 &&
//                Double.compare(that.d2ask, d2ask) != 0 &&
                        Double.compare(that.d2askVolume, d2askVolume) != 0 &&
//                Double.compare(that.d2bid, d2bid) != 0 &&
                        Double.compare(that.d2bidVolume, d2bidVolume) != 0);
    }
    @Override
    public int hashCode() {
        return Objects.hash(orderMap, d1ask, d1askVolume, d1bid, d1bidVolume, d2ask, d2askVolume, d2bid, d2bidVolume);
    }

}
