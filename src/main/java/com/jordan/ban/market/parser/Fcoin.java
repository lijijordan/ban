package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.domain.Ticker;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


@Log
public class Fcoin extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Fcoin";

    private static String DEPTH_URL_TEMPLATE = "https://api.fcoin.com/v2/market/depth/L20/%s";

    @Override
    public String getName() {
        return PLATFORM_NAME;
    }

    @Override
    public Symbol getPrice(String symbol) {
        return null;
    }

    @Override
    public Depth getDepth(String symbol) {
        symbol = symbol.toLowerCase();
        JSONObject jsonObject = null;
        try {
            jsonObject = super.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol)).getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = System.currentTimeMillis();
            JSONArray bids = jsonObject.getJSONArray("bids");
            JSONArray asks = jsonObject.getJSONArray("asks");
            parseOrder(bidList, bids);
            parseOrder(askList, asks);
            depth.setBids(bidList);
            depth.setAsks(askList);
            depth.setPlatform(PLATFORM_NAME);
            depth.setSymbol(symbol);
            depth.setTime(new Date(time));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return depth;
    }

    private void parseOrder(List<Ticker> tickers, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i = i + 2) {
            double price = jsonArray.getDouble(i);
            double size = jsonArray.getDouble(i + 1);
            Ticker order1 = new Ticker();
            order1.setPrice(price);
            order1.setVolume(size);
            tickers.add(order1);
        }
    }


    public static void main(String[] args) {
        System.out.println(JSONUtil.toJsonString(new Fcoin().getDepth("BTCUSDT")));
    }
}
