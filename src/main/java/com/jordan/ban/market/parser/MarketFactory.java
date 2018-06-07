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
        if (market.equals(Gateio.class.getSimpleName())) {
            return new Gateio();
        }
        if (market.equals(BitZ.class.getSimpleName())) {
            return new BitZ();
        }
        if (market.equals(Exmo.class.getSimpleName())) {
            return new Exmo();
        }
        return null;
    }
}
