package com.jordan.ban.market.parser;

import com.jordan.ban.common.KeyUtil;

import java.security.KeyException;
import java.util.concurrent.ConcurrentHashMap;

import static com.jordan.ban.common.Constant.KEY;
import static com.jordan.ban.common.Constant.KEY_SEC;

public class MarketFactory {

    private static ConcurrentHashMap<String, MarketParser> instanceMap = new ConcurrentHashMap<>();

    public static MarketParser getMarket(String market) {
        if (instanceMap.get(market) != null) {
            return instanceMap.get(market);
        }

        String key = null, sec = null;
        try {
            key = KeyUtil.getKey(market, KEY);
            sec = KeyUtil.getKey(market, KEY_SEC);
        } catch (KeyException e) {
            e.printStackTrace();
        }
        MarketParser marketParser = null;
        if (market.equals(Huobi.class.getSimpleName())) {
            marketParser = new Huobi();
        }
        if (market.equals(Dragonex.class.getSimpleName())) {
            marketParser = new Dragonex(key, sec);
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
            marketParser = new Fcoin(key, sec);
        }
        instanceMap.put(market, marketParser);
        return marketParser;
    }

    public static void main(String[] args) {
        getMarket(Fcoin.PLATFORM_NAME);
    }
}
