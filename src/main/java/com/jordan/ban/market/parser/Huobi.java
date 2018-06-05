package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.Ticker;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.http.HttpClientFactory;
import com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl;
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


@Log
public class Huobi extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Huobi";

    private static String PRICE_URL_TEMPLATE = "https://api.huobipro.com/market/detail/merged?symbol=%s";
    private static String DEPTH_URL_TEMPLATE = "https://api.huobipro.com/market/depth?symbol=%s&type=step1";

    @Override
    public String getName() {
        return PLATFORM_NAME;
    }

    @Override
    public Symbol getPrice(String symbol) {
        symbol = symbol.toLowerCase();
        String url = String.format(PRICE_URL_TEMPLATE, symbol);
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

    /**
     * {
     * "status": "ok",
     * "ch": "market.eosusdt.depth.step1",
     * "ts": 1527570796433,
     * "tick": {
     * "bids": [
     * [
     * 11.233300000000000000,
     * 1371.939900000000000000
     * ],
     * [
     * 11.2275000000000000
     *
     * @param symbol
     * @return
     */
    @Override
    public Depth getDepth(String symbol) {
        symbol = symbol.toLowerCase();
        JSONObject jsonObject = super.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol));
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = jsonObject.getLong("ts");
            JSONArray bids = jsonObject.getJSONObject("tick").getJSONArray("bids");
            JSONArray asks = jsonObject.getJSONObject("tick").getJSONArray("asks");
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
        Huobi huobi = new Huobi();
        System.out.println(huobi.getDepth("EOSUSDT"));
    }
}
