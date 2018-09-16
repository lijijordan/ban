package com.jordan.ban.market.parser;

import com.jordan.ban.common.KeyUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.security.KeyException;
import java.util.concurrent.ConcurrentHashMap;

import static com.jordan.ban.common.Constant.KEY;
import static com.jordan.ban.common.Constant.KEY_SEC;


@Slf4j
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
            try {
                marketParser = new Dragonex(key, sec);
            } catch (URISyntaxException e) {
                log.error("Initial dragonex websocket failed.");
                e.printStackTrace();
            }
        }
        if (market.equals(Okex.class.getSimpleName())) {
            marketParser = new Okex(null);
        }
        if (market.equals(Gateio.class.getSimpleName())) {
            marketParser = new Gateio();
        }
        if (market.equals(BitZ.class.getSimpleName())) {
            marketParser = new BitZ(null);
        }
        if (market.equals(Exmo.class.getSimpleName())) {
            marketParser = new Exmo(null);
        }
        if (market.equals(Fcoin.class.getSimpleName())) {
            try {
                marketParser = new Fcoin(key, sec);
            } catch (URISyntaxException e) {
                log.error("Initial fcoin websocket failed.");
                e.printStackTrace();
            }
        }
        instanceMap.put(market, marketParser);
        return marketParser;
    }

    public static void main(String[] args) {
        getMarket(Fcoin.PLATFORM_NAME);
    }
}
