package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Stacks;
import com.jordan.ban.domain.Symbol;
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
import java.util.Date;

@Log
public class Dragonex implements MarketParser {

    private static final String URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/real/?symbol_id=%s";

    private static final String PLATFORM_NAME = "Dragonex";


    @Override
    public Symbol getPrice(String symbol) {
        int symbolId = 0;
        // fixme : https://openapi.dragonex.im/api/v1/symbol/all/
        switch (symbol.toLowerCase()) {
            case "neousdt":
                symbolId = 129;
                break;
            case "eosusdt":
                symbolId = 113;
                break;
        }
        String url = String.format(URL_TEMPLATE, symbolId);
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

    @Override
    public Stacks getStacks(String symbol) {
        return null;
    }
}
