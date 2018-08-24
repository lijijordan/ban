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
@Builder
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

    public static MarketDepth parse(String json) {
        return JSONUtil.getEntity(json, MarketDepth.class);
    }

    public boolean notEqualsAll(Object o) {
        MarketDepth that = (MarketDepth) o;
        return (Double.compare(that.d1ask, d1ask) != 0 &&
                Double.compare(that.d1askVolume, d1askVolume) != 0 &&
                Double.compare(that.d1bid, d1bid) != 0 &&
                Double.compare(that.d1bidVolume, d1bidVolume) != 0 &&
                Double.compare(that.d2ask, d2ask) != 0 &&
                Double.compare(that.d2askVolume, d2askVolume) != 0 &&
                Double.compare(that.d2bid, d2bid) != 0 &&
                Double.compare(that.d2bidVolume, d2bidVolume) != 0);
    }
    @Override
    public int hashCode() {

        return Objects.hash(orderMap, d1ask, d1askVolume, d1bid, d1bidVolume, d2ask, d2askVolume, d2bid, d2bidVolume);
    }

    public static void main(String[] args) {
        MarketDepth m1 = MarketDepth.builder().d1ask(1).d1askVolume(2).d2ask(3).d2askVolume(4).d1bid(5).d1bidVolume(6).d2bid(7).d2bidVolume(8).build();
//        MarketDepth m2 = MarketDepth.builder().d1ask(2).d1askVolume(2).d2ask(3).d2askVolume(4).d1bid(5).d1bidVolume(6).d2bid(7).d2bidVolume(8).build();
        MarketDepth m2 = MarketDepth.builder().d1ask(2).build();

//        m1.setD1ask(1.1);
        System.out.println(m2.notEqualsAll(m1));
    }
}
