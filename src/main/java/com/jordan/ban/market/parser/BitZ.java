package com.jordan.ban.market.parser;

import com.jordan.ban.domain.*;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


@Log
public class BitZ extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "BitZ";

    private static String PRICE_URL_TEMPLATE = "";
    private static String DEPTH_URL_TEMPLATE = "https://www.bit-z.com/api_v1/depth?coin=%s";

    public BitZ(URI serverUri) {
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
//        JSONObject jsonObject = super.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol));
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject("{\"code\":0,\"msg\":\"Success\",\"data\":{\"asks\":[[\"0.10000000\",\"1.0000\"],[\"0.03165066\",\"7.5031\"],[\"0.01888888\",\"15.0000\"],[\"0.00862068\",\"6.7040\"],[\"0.00800000\",\"100.0000\"],[\"0.00482758\",\"5.0000\"],[\"0.00400000\",\"3.0969\"],[\"0.00388000\",\"90.0000\"],[\"0.00362068\",\"5.0000\"],[\"0.00310300\",\"5.0000\"],[\"0.00300000\",\"101.5267\"],[\"0.00294900\",\"20.0000\"],[\"0.00275000\",\"4.0441\"],[\"0.00268600\",\"5.0000\"],[\"0.00257759\",\"1.0000\"],[\"0.00255500\",\"581.9000\"],[\"0.00255000\",\"100.1808\"],[\"0.00250000\",\"191.3016\"],[\"0.00249200\",\"6.8433\"],[\"0.00243000\",\"54.6930\"],[\"0.00240000\",\"87.8922\"],[\"0.00239000\",\"124.2393\"],[\"0.00232500\",\"50.8751\"],[\"0.00232100\",\"8.0848\"],[\"0.00230000\",\"5.3074\"],[\"0.00228500\",\"14.3178\"],[\"0.00223000\",\"3.5764\"],[\"0.00219000\",\"33.1318\"],[\"0.00210000\",\"122.2149\"],[\"0.00209760\",\"6.6300\"],[\"0.00199999\",\"207.1000\"],[\"0.00198600\",\"14.0000\"],[\"0.00198599\",\"35.0528\"],[\"0.00195315\",\"15.1041\"],[\"0.00194677\",\"26.4041\"],[\"0.00189100\",\"104.0200\"],[\"0.00187900\",\"46.0100\"],[\"0.00187847\",\"50.0000\"],[\"0.00187843\",\"81.3898\"]],\"bids\":[[\"0.00183800\",\"35.3000\"],[\"0.00183000\",\"154.0320\"],[\"0.00180601\",\"1.0000\"],[\"0.00180600\",\"22.5160\"],[\"0.00180200\",\"111.6542\"],[\"0.00180000\",\"19.0041\"],[\"0.00179700\",\"104.0700\"],[\"0.00179600\",\"842.2090\"],[\"0.00179500\",\"367.4600\"],[\"0.00179200\",\"842.5526\"],[\"0.00177349\",\"1.1337\"],[\"0.00177250\",\"2.0682\"],[\"0.00177241\",\"3.9918\"],[\"0.00177222\",\"7.8738\"],[\"0.00177221\",\"1.8723\"],[\"0.00177220\",\"11.5543\"],[\"0.00177211\",\"8.9870\"],[\"0.00177191\",\"2.0991\"],[\"0.00177182\",\"10.0374\"],[\"0.00177181\",\"5.6718\"],[\"0.00177171\",\"6.0547\"],[\"0.00177162\",\"7.5357\"],[\"0.00177152\",\"7.5138\"],[\"0.00177143\",\"6.9669\"],[\"0.00177142\",\"5.9206\"],[\"0.00177132\",\"5.4427\"],[\"0.00177122\",\"7.7168\"],[\"0.00177113\",\"2.0765\"],[\"0.00177112\",\"8.6583\"],[\"0.00177104\",\"1.2159\"],[\"0.00177084\",\"7.0657\"],[\"0.00177074\",\"6.0656\"],[\"0.00177073\",\"7.6319\"],[\"0.00177072\",\"7.5048\"],[\"0.00177044\",\"3.0289\"],[\"0.00177034\",\"6.3270\"],[\"0.00177014\",\"2.7827\"],[\"0.00177004\",\"7.8845\"],[\"0.00176994\",\"5.6358\"],[\"0.00176965\",\"8.7511\"],[\"0.00176955\",\"4.9080\"],[\"0.00176945\",\"9.2472\"],[\"0.00176935\",\"9.3758\"],[\"0.00176895\",\"8.1317\"],[\"0.00176718\",\"2.9001\"],[\"0.00176541\",\"11.4469\"],[\"0.00176364\",\"13.2542\"],[\"0.00176187\",\"14.5021\"],[\"0.00176010\",\"6.4622\"],[\"0.00176000\",\"20.0000\"],[\"0.00175833\",\"3.2702\"],[\"0.00175657\",\"18.6438\"],[\"0.00175481\",\"17.8188\"],[\"0.00175305\",\"6.9805\"],[\"0.00175129\",\"9.4670\"],[\"0.00175000\",\"29.0994\"],[\"0.00174953\",\"1.7178\"],[\"0.00174778\",\"15.0889\"],[\"0.00174603\",\"7.8772\"],[\"0.00174428\",\"10.6350\"],[\"0.00174253\",\"5.7451\"],[\"0.00174078\",\"8.5592\"],[\"0.00173903\",\"6.0380\"],[\"0.00173729\",\"2.6189\"],[\"0.00173555\",\"18.6822\"],[\"0.00173381\",\"14.6679\"],[\"0.00173207\",\"16.5411\"],[\"0.00173033\",\"7.1668\"],[\"0.00172859\",\"1.2011\"],[\"0.00172686\",\"19.0720\"],[\"0.00172513\",\"12.4167\"],[\"0.00172340\",\"16.6280\"],[\"0.00172167\",\"6.2205\"],[\"0.00172000\",\"100.0000\"],[\"0.00170000\",\"460.8482\"],[\"0.00167300\",\"2.1536\"],[\"0.00167167\",\"1.3341\"],[\"0.00165200\",\"3.5584\"],[\"0.00164700\",\"3.2345\"],[\"0.00163000\",\"1.0116\"],[\"0.00162000\",\"10.1952\"],[\"0.00161400\",\"10.1899\"],[\"0.00161200\",\"10.2025\"],[\"0.00161000\",\"4.0992\"],[\"0.00160100\",\"263.7058\"],[\"0.00158244\",\"4.0478\"],[\"0.00156010\",\"3.3035\"],[\"0.00155000\",\"2.3520\"],[\"0.00154000\",\"20.0000\"],[\"0.00151942\",\"1.0000\"],[\"0.00145700\",\"18.3511\"],[\"0.00144201\",\"50.0000\"],[\"0.00144200\",\"443.0714\"],[\"0.00143588\",\"9.8878\"],[\"0.00143000\",\"3.8430\"],[\"0.00142500\",\"1.8226\"],[\"0.00140000\",\"1.0000\"],[\"0.00136498\",\"17.0177\"],[\"0.00130000\",\"100.0000\"],[\"0.00116498\",\"9.9697\"]],\"date\":1528251187}}");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = System.currentTimeMillis();
            JSONArray bids = jsonObject.getJSONObject("data").getJSONArray("bids");
            JSONArray asks = jsonObject.getJSONObject("data").getJSONArray("asks");
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
