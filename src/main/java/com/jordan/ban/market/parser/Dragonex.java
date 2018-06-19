package com.jordan.ban.market.parser;

import com.jordan.ban.domain.*;
import com.jordan.ban.http.HttpClientFactory;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * https://github.com/huobiapi/API_Docs/wiki/REST_api_reference
 */
@Log
public class Dragonex extends BaseMarket implements MarketParser {

    private static final String PRICE_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/real/?symbol_id=%s";
    private static final String BIDS_DEPTH_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/sell/?symbol_id=%s";
    private static final String ASKS_DEPTH_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/buy/?symbol_id=%s";

    public static final String PLATFORM_NAME = "Dragonex";

    @Override
    public String getName() {
        return PLATFORM_NAME;
    }

    @Override
    public Symbol getPrice(String symbol) {
        int symbolId = getSymbolId(symbol);
        String url = String.format(PRICE_URL_TEMPLATE, symbolId);
        log.info("load url:" + url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) HttpClientFactory.getHttpClient().execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();
        String body = null;
        Symbol s1 = new Symbol();
        try {
            body = EntityUtils.toString(entity, "UTF-8");
            JSONObject jsonObject = new JSONObject(body);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                s1.setCreateTime(new Date(object.getLong("timestamp") * 1000));
                s1.setPlatform(PLATFORM_NAME);
                s1.setPrice(object.getDouble("close_price"));
                s1.setSymbol(symbol);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return s1;
    }

    /*
     https://openapi.dragonex.im/api/v1/market/buy/?symbol_id=129
{
  "ok": true,
  "code": 1,
  "data": [
    {
      "price": "48.3244",
      "volume": "0.0428"
    },
    {
      "price": "48.0000",
      "volume": "0.8367"
    },
     * @param symbol
     * @return
     */
    @Override
    public Depth getDepth(String symbol) {
        Depth depth = new Depth();
        depth.setTime(new Date());
        depth.setSymbol(symbol);
        depth.setPlatform(PLATFORM_NAME);
        try {
            depth.setBids(getBids(symbol));
            depth.setAsks(getAsks(symbol));
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
    public String placeOrder(OrderRequest request) {
        return null;
    }

    @Override
    public OrderResponse getFilledOrder(String orderId) {
        return null;
    }

    @Override
    public boolean cancelOrder(String orderId) {
        return false;
    }


    private List<Ticker> getBids(String symbol) throws JSONException {
        return getOrders(symbol, BIDS_DEPTH_URL_TEMPLATE);
    }


    private List<Ticker> getAsks(String symbol) throws JSONException {
        return getOrders(symbol, ASKS_DEPTH_URL_TEMPLATE);
    }

    private List<Ticker> getOrders(String symbol, String asksDepthUrlTemplate) throws JSONException {
        List<Ticker> list = new ArrayList<>();
        int symbolId = getSymbolId(symbol);
        JSONObject jsonObject = super.parseJSONByURL(String.format(asksDepthUrlTemplate, symbolId));
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        for (int i = 0; i < jsonArray.length(); i++) {
            Ticker order = new Ticker();
            JSONObject obj = (JSONObject) jsonArray.get(i);
            double price = obj.getDouble("price");
            double volume = obj.getDouble("volume");
            order.setPrice(price);
            order.setVolume(volume);
            list.add(order);
        }
        return list;
    }

    // fixme : https://openapi.dragonex.im/api/v1/symbol/all/
    private int getSymbolId(String symbol) {
        int symbolId = 0;
        switch (symbol.toLowerCase()) {
            case "neousdt":
                symbolId = 129;
                break;
            case "eosusdt":
                symbolId = 113;
                break;
            case "btcusdt":
                symbolId = 101;
                break;
            case "eoseth":
                symbolId = 1130103;
                break;
            case "gxseth":
                symbolId = 1400103;
                break;
            case "bchusdt":
                symbolId = 111;
                break;
        }
        return symbolId;
    }

    public static void main(String[] args) {
        Dragonex dragonex = new Dragonex();
        System.out.println(dragonex.getDepth("EOSUSDT").toString());
    }
}
