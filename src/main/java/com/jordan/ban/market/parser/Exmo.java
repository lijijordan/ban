package com.jordan.ban.market.parser;

import com.jordan.ban.domain.*;
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
public class Exmo extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Exmo";

    private static String DEPTH_URL_TEMPLATE = "https://api.exmo.com/v1/order_book/?pair=%s";

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
        symbol = symbol.toUpperCase().replace("USDT", "USD");
        symbol = symbol.substring(0, 3) + "_" + symbol.substring(3, symbol.length());
        JSONObject jsonObject = null;
        try {
            jsonObject = super.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol)).getJSONObject(symbol);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = System.currentTimeMillis();
            JSONArray bids = jsonObject.getJSONArray("bid");
            JSONArray asks = jsonObject.getJSONArray("ask");
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

    @Override
    public BalanceDto getBalance(String symbol) {
        return null;
    }

    @Override
    public Long placeOrder(OrderRequest request) {
        return null;
    }

    @Override
    public OrderResponse getFilledOrder(long orderId) {
        return null;
    }


    private void parseOrder(List<Ticker> tickers, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray order = (JSONArray) jsonArray.get(i);
            double price = order.getDouble(0);
            double size = order.getDouble(1);
            Ticker order1 = new Ticker();
            order1.setPrice(price);
            order1.setVolume(size);
            tickers.add(order1);
        }
    }


    public static void main(String[] args) {
        System.out.println(JSONUtil.toJsonString(new Exmo().getDepth("EOSUSDT")));
    }
}
