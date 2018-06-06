package com.jordan.ban.market.trade;

import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.domain.MockTradeResult;
import com.jordan.ban.domain.TradeDirect;

public class TradeHelper {

    // 交易手续费
    private static final double TRADE_FEES = 0.002;

    public static MockTradeResult eatA2B(MarketDepth marketDepth) {
        double tradeBuy = Math.max(marketDepth.getD1ask(), marketDepth.getD1bid());
        double tradeSell = Math.min(marketDepth.getD2ask(), marketDepth.getD2bid());
        double minVolume = Math.min(marketDepth.getVolume(tradeBuy), marketDepth.getVolume(tradeSell));
        double tradeDiff = ((tradeSell - tradeBuy) - Math.abs(Math.max(tradeBuy, tradeSell) * TRADE_FEES * 2));
        return new MockTradeResult(tradeDiff, tradeDiff / Math.max(tradeBuy, tradeSell), TradeDirect.A2B, minVolume);
    }

    public static MockTradeResult eatB2A(MarketDepth marketDepth) {
        double tradeBuy = Math.max(marketDepth.getD2ask(), marketDepth.getD2bid());
        double tradeSell = Math.min(marketDepth.getD1ask(), marketDepth.getD1bid());
        double minVolume = Math.min(marketDepth.getVolume(tradeBuy), marketDepth.getVolume(tradeSell));
        double tradeDiff = ((tradeSell - tradeBuy) - Math.abs(Math.max(tradeBuy, tradeSell) * TRADE_FEES * 2));
        return new MockTradeResult(tradeDiff, tradeDiff / Math.max(tradeBuy, tradeSell), TradeDirect.B2A, minVolume);
    }


    /**
     * 利益最大化
     *
     * @param marketDepth
     * @return
     */
    public static MockTradeResult tradeA2B(MarketDepth marketDepth) {
//        System.out.println(marketDepth.toString());
        double minVolume = 1;
        double tradeBuy = Math.min(marketDepth.getD1ask(), marketDepth.getD1bid()) * minVolume;
        double tradeSell = Math.max(marketDepth.getD2ask(), marketDepth.getD2bid()) * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
//        System.out.println(String.format("D1_卖1：%s, D2_买1：%s", marketDepth.getD1ask(),
//                marketDepth.getD2bid()));
//        System.out.println("Market 1 buy , Market 2 sell. diff=" + (float) tradeDiff);
        // TODO:减去手续费
        tradeDiff = tradeDiff - Math.abs(Math.max(tradeBuy, tradeSell) * TRADE_FEES * 2);
        return new MockTradeResult(tradeDiff, tradeDiff / Math.max(tradeBuy, tradeSell),
                TradeDirect.A2B, Math.min(marketDepth.getVolume(tradeBuy), marketDepth.getVolume(tradeSell)));
    }

    public static MockTradeResult tradeB2A(MarketDepth marketDepth) {
        double minVolume = 1;
        double tradeBuy = Math.min(marketDepth.getD2ask(), marketDepth.getD2bid()) * minVolume;
        double tradeSell = Math.max(marketDepth.getD1ask(), marketDepth.getD1bid()) * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
//        System.out.println(String.format("D2_卖1：%s, D1_买1：%s", marketDepth.getD2ask(),
//                marketDepth.getD1bid()));
//        System.out.println("Market 1 sell , Market 2 buy. diff=" + (float) tradeDiff);
        // TODO:减去手续费
        tradeDiff = tradeDiff - Math.abs(Math.max(tradeBuy, tradeSell) * TRADE_FEES * 2);
        return new MockTradeResult(tradeDiff, tradeDiff / Math.max(tradeBuy, tradeSell),
                TradeDirect.B2A, Math.min(marketDepth.getVolume(tradeBuy), marketDepth.getVolume(tradeSell)));
    }


}
