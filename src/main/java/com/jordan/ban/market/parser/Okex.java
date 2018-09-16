package com.jordan.ban.market.parser;

import com.jordan.ban.domain.*;
import com.jordan.ban.http.HttpClientFactory;
import com.jordan.ban.utils.JSONUtil;
import com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


@Log
public class Okex extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Okex";

    private static String PRICE_URL_TEMPLATE = "";
    private static String DEPTH_URL_TEMPLATE = "https://www.okex.com/api/v1/depth.do?symbol=%s";

    public Okex(URI serverUri) {
        super(serverUri);
    }

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
        symbol = symbol.substring(0, 3) + "_" + symbol.substring(3, symbol.length());
        symbol = symbol.toLowerCase();
        JSONObject jsonObject = super.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol));
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = System.currentTimeMillis();
            JSONArray bids = jsonObject.getJSONArray("bids");
            JSONArray asks = jsonObject.getJSONArray("asks");
            parseOrder(bidList, bids);
            parseOrder(askList, asks);

            //OK Ask返回结果重新排序
            askList.sort(Comparator.comparingDouble(Ticker::getPrice));
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
        String symbol = "neousdt";
        symbol = symbol.substring(0, 3) + "_" + symbol.substring(3, symbol.length());
        System.out.println(symbol);
//        System.out.println(JSONUtil.toJsonString(new Okex().getDepth("EOS_USDT")));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
