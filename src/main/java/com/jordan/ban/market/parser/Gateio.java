package com.jordan.ban.market.parser;

import com.jordan.ban.domain.*;
import com.jordan.ban.market.gateio.HttpUtilManager;
import com.jordan.ban.market.gateio.IStockRestApi;
import com.jordan.ban.market.gateio.StockRestApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;


@Slf4j
public class Gateio extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Gateio";
    private static String DEPTH_URL_TEMPLATE = "https://data.gateio.io/api2/1/orderBook/%s";

    public static final String TRADE_URL = "https://api.gateio.io";
    private String accessKeyId;
    private String accessKeySecret;

    IStockRestApi stockPost;

    public Gateio(String accessKeyId, String accessKeySecret) {
        super(null);
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        stockPost = new StockRestApi(TRADE_URL);
        HttpUtilManager.getInstance().initKey(this.accessKeyId, this.accessKeySecret);
    }

    public Gateio() {
        super(null);
    }

    @Override
    public String getName() {
        return PLATFORM_NAME;
    }

    @Override
    public Symbol getPrice(String symbol) {
        return null;
    }

    private String convertSymbol(String symbol) {
        if (StringUtils.isEmpty(symbol) || symbol.indexOf("_") != -1) {
            return symbol;
        }
        symbol = symbol.substring(0, 3) + "_" + symbol.substring(3, symbol.length());
        symbol = symbol.toLowerCase();
        return symbol;
    }

    @Override
    public Depth getDepth(String symbol) {
        symbol = this.convertSymbol(symbol);
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


    public List<BalanceDto> getBalances() {
        String response = null;
        List<BalanceDto> list = new ArrayList<>();
        try {
            response = this.stockPost.balance();
            log.info(response);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject != null && jsonObject.getString("result").equals("true")) {
                JSONObject available = jsonObject.getJSONObject("available");
                JSONObject locked = jsonObject.getJSONObject("locked");
                Iterator<String> it = available.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    String availableValue = available.getString(key).toLowerCase();
                    String lockedValue = locked.getString(key);
                    list.add(BalanceDto.builder().currency(key).balance(Double.valueOf(availableValue) + Double.valueOf(lockedValue))
                            .frozen(Double.valueOf(lockedValue)).available(Double.valueOf(availableValue)).build());

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public BalanceDto getBalance(String symbol) {
        symbol = this.convertSymbol(symbol);
        return null;
    }

    /**
     * {"result":"true","message":"Success","code":0,"orderNumber":946102022
     * ,"rate":"7.58","leftAmount":"0.20000000","filledAmount":"0","filledRate":"7.58"}
     *
     * @param request
     * @return
     */
    @Override
    public String placeOrder(OrderRequest request) {
        String symbol = this.convertSymbol(request.getSymbol());
        String response = null, id = null;
        try {
            if (request.getType() == OrderType.BUY_LIMIT) {
                response = stockPost.buy(symbol, String.valueOf(request.getPrice()),
                        String.valueOf(request.getAmount()));
            } else {
                response = stockPost.sell(symbol, String.valueOf(request.getPrice()),
                        String.valueOf(request.getAmount()));
            }
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("respone:{}", response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            id = String.valueOf(jsonObject.getInt("orderNumber"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * {"result":"true","order":{"orderNumber":"946157366","status":"open","currencyPair":"eos_usdt","type":"buy","rate":"7.5801","amount":"0.20000000","initialRate":"7.5801","initialAmount":"0.2","filledAmount":"0","filledRate":"7.5801","feePercentage":0.2,"feeValue":"0","feeCurrency":"USDT","fee":"0 USDT","timestamp":1530090833},"message":"Success","code":0,"elapsed":"1.10984ms"}
     * @param orderId
     * @param symbol
     * @return
     */
    private OrderResponse getFilledOrder(String orderId, String symbol) {
        symbol = this.convertSymbol(symbol);
        String response = null;
        try {
            response = stockPost.getOrder(orderId, symbol);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("response:{}", response);
        return null;
    }

    @Override
    public OrderResponse getFilledOrder(String orderId) {
        this.getFilledOrder(orderId, "");
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


    public static void main(String[] args) throws IOException, HttpException {
        String symbol = "eosusdt";
        Gateio gateio = (Gateio) MarketFactory.getMarket(Gateio.PLATFORM_NAME);


//        System.out.println(gateio.getBalances());

        // place order
        /*OrderRequest request = OrderRequest.builder().type(OrderType.BUY_LIMIT)
                .symbol(symbol).price(7.5801).amount(0.2).build();
        String orderId = gateio.placeOrder(request);*/


        // query order  :946157366
        gateio.getFilledOrder("946157366", symbol);
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