package com.jordan.ban;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.Symbol;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * User: liji
 * Date: 18/5/16
 * Time: 下午2:07
 */
@Slf4j
public class CompareApplicationTest {

    private static final CloseableHttpClient client = HttpClients.createDefault();

    private static final long WAIT_TIME = 500;

    public static Map<String, Symbol> parseLBankTickers() throws IOException, JSONException {
        Map<String, Symbol> map = new HashMap<>();
//        export http_proxy="http://127.0.0.1:8001"; export HTTP_PROXY="http://127.0.0.1:8001"; export https_proxy="http://127.0.0.1:8001"; export HTTPS_PROXY="http://127.0.0.1:8001"
        HttpHost proxy = new HttpHost("127.0.0.1", 8001);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
        CloseableHttpClient closeableHttpClient = HttpClients.custom()
                .setRoutePlanner(routePlanner)
                .build();

        HttpGet httpGet = new HttpGet("https://api.lbank.info/v1/ticker.do?symbol=all");
        httpGet.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity, "UTF-8");
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject record = (JSONObject) jsonArray.get(i);
            String marketName = record.getString("symbol");
            String symbolArray[] = marketName.toUpperCase().split("_");
            if (symbolArray != null && symbolArray.length > 1) {
                Symbol symbol = new Symbol();
                String s1 = symbolArray[0] + symbolArray[1];
                symbol.setSymbol(s1);
                symbol.setPlatform("LBank");
                symbol.setPrice(record.getJSONObject("ticker").getDouble("latest"));
                // FIXME use server time:"Created": "2017-08-04T16:58:43.087"
                symbol.setTime(new Date(System.currentTimeMillis()));
                map.put(symbol.getSymbol(), symbol);
            }
        }
        closeableHttpClient.close();
        return map;
    }

    /**
     * https://bittrex.com/api/v1.1/public/getmarketsummaries
     *
     * @return map
     * @throws IOException   the io exception
     * @throws JSONException the json exception
     */
    public static Map<String, Symbol> parseBittrexTickers() throws IOException, JSONException {
        Map<String, Symbol> map = new HashMap<>();
        HttpGet httpGet = new HttpGet("https://bittrex.com/api/v1.1/public/getmarketsummaries");
        httpGet.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonObject = new JSONObject(body);
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject record = (JSONObject) jsonArray.get(i);
            String marketName = record.getString("MarketName");
            String symbolArray[] = marketName.split("-");
            if (symbolArray != null && symbolArray.length > 1) {
                Symbol symbol = new Symbol();
                String s1 = symbolArray[1] + symbolArray[0];
                symbol.setSymbol(s1);
                symbol.setPlatform("Bittrex");
                symbol.setPrice(record.getDouble("Last"));
                // FIXME use server time:"Created": "2017-08-04T16:58:43.087"
                symbol.setTime(new Date(System.currentTimeMillis()));
                map.put(symbol.getSymbol(), symbol);
            }
        }
        return map;
    }

    /**
     * Parse binance tickers map.
     *
     * @return the map
     * @throws IOException   the io exception
     * @throws JSONException the json exception
     */
    public static Map<String, Symbol> parseBinanceTickers() throws IOException, JSONException {
        Map<String, Symbol> map = new HashMap<>();
        HttpGet httpGet = new HttpGet("https://api.binance.com//api/v3/ticker/price");
        httpGet.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity, "UTF-8");
        JSONArray jsonArray = new JSONArray(body);
        for (int i = 0; i < jsonArray.length(); i++) {
            Symbol symbol = new Symbol();
            symbol.setPlatform("Binance");
            symbol.setTime(new Date(System.currentTimeMillis()));
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String s = jsonObject.get("symbol").toString();
            String p = jsonObject.get("price").toString();
            symbol.setSymbol(s);
            symbol.setPrice(Double.valueOf(p));
            map.put(s, symbol);
        }
        return map;
    }

    /**
     * Parse otc btc tickers map.
     *
     * @return the map
     * @throws IOException   the io exception
     * @throws JSONException the json exception
     */
    public static Map<String, Symbol> parseOtcBtcTickers() throws IOException, JSONException {
        Map<String, Symbol> map = new HashMap<>();
        HttpGet httpGet = new HttpGet("https://bb.otcbtc.com/api/v2/tickers");
        httpGet.setHeader("Content-Type", "application/json");
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonObject = new JSONObject(body);
        Iterator keys = jsonObject.keys();
        while (keys.hasNext()) {
            Symbol symbol = new Symbol();
            symbol.setPlatform("OTCBTC");
            symbol.setTime(new Date(System.currentTimeMillis()));
            String s = (String) keys.next();
            JSONObject object = (JSONObject) jsonObject.get(s);
            double price = ((JSONObject) object.get("ticker")).getDouble("last");
            symbol.setPrice(price);
            symbol.setSymbol(s.toUpperCase().replace("_", ""));
            map.put(symbol.getSymbol(), symbol);
        }
        return map;
    }

    public static void runCompare(Map<String, Symbol> map1, Map<String, Symbol> map2) {
        new Thread(() -> {
            long max = 0, min = 1000, avg, current, sum = 0;
            int i = 0;
            while (true) {
                i++;
                try {
                    long start = System.currentTimeMillis();
                    try {
                        Thread.sleep(WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    comparePrice(map1, map2);
                    long end = System.currentTimeMillis();
                    current = end - start;
                    if (max < current) {
                        max = current;
                    }
                    if (min > current) {
                        min = current;
                    }
                    sum = sum + current;
                    avg = sum / i;
                    System.out.println(String.format("Compare method cost time:【%s】ms, " +
                            "avg:【%s】ms, max:【%s】ms, min:【%s】ms", current, avg, max, min));
                } catch (Exception e) {
                    try {
                        client.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }


    /**
     * The entry point of application.
     *
     * @param args the input arguments
     * @throws IOException   the io exception
     * @throws JSONException the json exception
     */
    public static void main(String[] args) throws IOException, JSONException {
//        ElasticSearchClient.initClient("localhost", 9300);
//
//        runCompare(parseBinanceTickers(), parseOtcBtcTickers());
//        runCompare(parseLBankTickers(), parseBittrexTickers());
//        runCompare(parseBittrexTickers(), parseOtcBtcTickers());


        new Thread(() -> {
            long max = 0, min = 10000, avg, current, sum = 0;
            int i = 0;
            while (true) {
                i++;
                try {
                    long start = System.currentTimeMillis();
                    comparePrice(parseBittrexTickers(), parseBinanceTickers());
                    long end = System.currentTimeMillis();
                    current = end - start;
                    if (max < current) {
                        max = current;
                    }
                    if (min > current) {
                        min = current;
                    }
                    sum = sum + current;
                    avg = sum / i;
                    System.out.println(String.format("Compare method cost time:【%s】ms, " +
                            "avg:【%s】ms, max:【%s】ms, min:【%s】ms , time: %s", current, avg, max, min, new Date()));
                } catch (Exception e) {
                    try {
                        client.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();

        while (true) {
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("========================= tick ===========================");
        }
    }

    /**
     * Compare price.
     *
     * @param m1 the m 1
     * @param m2 the m 2
     */
    public static void comparePrice(Map<String, Symbol> m1, Map<String, Symbol> m2) {
        List<Differ> differs = new ArrayList<>();
        m1.forEach((m, k) -> {
            String symbol = m;
            Symbol symbol1 = k, symbol2 = m2.get(symbol);
            double price1 = k.getPrice();
            if (symbol2 != null) {
                double price2 = symbol2.getPrice();
                double differ = (price1 - price2) / Math.max(price1, price2);
                DecimalFormat df = new DecimalFormat("##.##%");
                String formattedPercent = df.format(differ);
//                System.out.println(String.format("Symbol:%s, Differ:%s", symbol, formattedPercent));
                Differ differObject = new Differ();
                differObject.setSymbol(symbol);
                differObject.setDiffer((float) differ);
                differObject.setPercentDiffer(formattedPercent);
                differObject.setCreateTime(new Date());
                differObject.setDifferPlatform(symbol1.getPlatform() + "-" + symbol2.getPlatform());
                differs.add(differObject);
//                ElasticSearElasticSearchClientchClient.index(Constant.INDEX_NAME, "data", JSONUtil.toJsonString(differObject));
            }
        });
    }
}
