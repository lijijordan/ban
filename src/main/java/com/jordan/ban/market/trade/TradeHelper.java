package com.jordan.ban.market.trade;

import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.domain.MockTradeResult;
import com.jordan.ban.domain.TradeDirect;

public class TradeHelper {

    public static MockTradeResult tradeA2B(MarketDepth marketDepth) {
        double minVolume = 1;
        double tradeBuy = marketDepth.getD1ask() * minVolume;
        double tradeSell = marketDepth.getD2bid() * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
        System.out.println("Market 1 buy , Market 2 sell. diff=" + (float) tradeDiff);
        return new MockTradeResult(tradeDiff, tradeDiff / (tradeBuy + tradeSell), TradeDirect.A2B);
    }

    public static MockTradeResult tradeB2A(MarketDepth marketDepth) {
        double minVolume = 1;
        double tradeBuy = marketDepth.getD2ask() * minVolume;
        double tradeSell = marketDepth.getD1bid() * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
        System.out.println("Market 1 sell , Market 2 buy. diff=" + (float) tradeDiff);
        return new MockTradeResult(tradeDiff, tradeDiff / (tradeBuy + tradeSell), TradeDirect.B2A);
    }
}
