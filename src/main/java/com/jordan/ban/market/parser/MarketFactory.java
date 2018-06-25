package com.jordan.ban.market.parser;

import java.util.concurrent.ConcurrentHashMap;

public class MarketFactory {

    private static ConcurrentHashMap<String, MarketParser> instanceMap = new ConcurrentHashMap<>();

    public static MarketParser getMarket(String market) {
        if (instanceMap.get(market) != null) {
            return instanceMap.get(market);
        }
        MarketParser marketParser = null;
        if (market.equals(Huobi.class.getSimpleName())) {
            marketParser = new Huobi();
        }
        if (market.equals(Dragonex.class.getSimpleName())) {
            marketParser = new Dragonex();
        }
        if (market.equals(Okex.class.getSimpleName())) {
            marketParser = new Okex();
        }
        if (market.equals(Gateio.class.getSimpleName())) {
            marketParser = new Gateio();
        }
        if (market.equals(BitZ.class.getSimpleName())) {
            marketParser = new BitZ();
        }
        if (market.equals(Exmo.class.getSimpleName())) {
            marketParser = new Exmo();
        }
        if (market.equals(Fcoin.class.getSimpleName())) {
            marketParser = new Fcoin();
        }
        instanceMap.put(market, marketParser);
        return marketParser;
    }
}
