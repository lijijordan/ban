package com.jordan.ban.market.parser;

public class MarketFactory {

    public static MarketParser getMarket(String market) {
        MarketParser marketParser = null;
        if (market.equals(Huobi.class.getSimpleName())) {
            return new Huobi();
        }
        if (market.equals(Dragonex.class.getSimpleName())) {
            return new Dragonex();
        }
        if (market.equals(Okex.class.getSimpleName())) {
            return new Okex();
        }
        return null;
    }
}
