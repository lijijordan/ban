package com.jordan.ban;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.utils.JSONUtil;

import java.io.IOException;
import java.util.Date;

/**
 * Market data stream endpoints examples.
 * <p>
 * It illustrates how to create a stream to obtain updates on market data such as executed trades.
 */
public class MarketDataStreamExample {

    public static void main(String[] args) throws InterruptedException, IOException {
        BinanceApiWebSocketClient client = BinanceApiClientFactory.newInstance().newWebSocketClient();
        ElasticSearchClient.initClient();
//     Listen for aggregated trade events for ETH/BTC
        client.onAggTradeEvent("eoseth", response -> {
            Symbol symbol = new Symbol();
            symbol.setCreateTime(new Date(response.getTradeTime()));
            symbol.setPlatform(Constant.BINANCE_NAME);
            symbol.setPrice(Double.valueOf(response.getPrice()));
            symbol.setSymbol(response.getSymbol().toUpperCase());
            System.out.println(symbol);
//            ElasticSearchClient.index(Constant.INDEX_NAME_SYMBOLS, "data", JSONUtil.toJsonString(symbol));
        });

        // Listen for changes in the order book in ETH/BTC
//    client.onDepthEvent("eoseth", response -> System.out.println(response));

        // Obtain 1m candlesticks in real-createTime for ETH/BTC
//    client.onCandlestickEvent("eoseth", CandlestickInterval.ONE_MINUTE, response -> System.out.println(response));


        /*client.onAllMarketTickersEvent(response -> {
            System.out.println(response);
        });*/
        int i = 0;
        while (true) {
            Thread.sleep(1000);
            i++;
            System.out.println("========================" + i + "======================");
        }
    }
}
