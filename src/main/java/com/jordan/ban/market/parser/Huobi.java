package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Stacks;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.http.HttpClientFactory;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;


@Log
public class Huobi implements MarketParser {

    public static final String PLATFORM_NAME = "Huobi";

    private static String URL_TEMPLATE = "https://api.huobipro.com/market/detail/merged?symbol=%s";

    @Override
    public Symbol getPrice(String symbol) {
        symbol = symbol.toLowerCase();
        String url = String.format(URL_TEMPLATE, symbol);
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
            JSONObject tick = jsonObject.getJSONObject("tick");
            s1.setSymbol(symbol.toUpperCase());
            s1.setPrice(tick.getDouble("close"));
            s1.setPlatform(PLATFORM_NAME);
            s1.setCreateTime(new Date(jsonObject.getLong("ts")));
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
