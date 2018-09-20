package com.jordan.ban.market.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jordan.ban.common.HttpParams;
import com.jordan.ban.common.HttpUtils;
import com.jordan.ban.domain.*;
import com.jordan.ban.exception.ApiException;
import com.jordan.ban.http.HttpClientFactory;
import com.jordan.ban.market.DepthHelper;
import com.jordan.ban.utils.ContextWrapper;
import com.jordan.ban.utils.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.jordan.ban.common.Constant.ETH_USDT;
import static com.jordan.ban.common.HttpUtils.sendPost;

@Slf4j
public class Dragonex extends BaseMarket implements MarketParser {

    private static final String PRICE_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/real/?symbol_id=%s";
    private static final String BIDS_DEPTH_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/sell/?symbol_id=%s";
    private static final String ASKS_DEPTH_URL_TEMPLATE = "https://openapi.dragonex.im/api/v1/market/buy/?symbol_id=%s";

    public static final String PLATFORM_NAME = "Dragonex";

    private volatile DragonexToken token;

    private ConcurrentHashMap<String, BalanceDto> balanceCache;

    private ConcurrentHashMap<String, DragonexSymbol> symbolCache;
    private ConcurrentHashMap<Integer, DragonexSymbol> symbolIdCache;

    private static final Long TOKE_15MIN_TIME = 15 * 60 * 1000l;

    private String accessKeyId;
    private String accessKeySecret;

    private Depth depthContext;

    private DepthHelper depthHelper;

    public Depth getDepthContext() {
        return depthContext;
    }

    private static final String WS_URL = "wss://openapiws.dragonex.im/ws";


    public Dragonex() throws URISyntaxException {
        super(new URI(WS_URL));
        this.init();
    }

    private void init() throws URISyntaxException {
        this.balanceCache = new ConcurrentHashMap();
        this.symbolCache = new ConcurrentHashMap();
        this.symbolIdCache = new ConcurrentHashMap();
        this.depthHelper = ContextWrapper.getContext().getBean(DepthHelper.class);
        this.initSymbol();
    }

    public Dragonex(String accessKeyId, String accessKeySecret) throws URISyntaxException {
        super(new URI(WS_URL));
        this.init();
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        try {
            this.setToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String MAIN_HOST = "https://openapi.dragonex.im";

    // get token
    public static String GET_TOKEN = "/api/v1/token/new/";

    // check token status(POST)
    public static String CHECK_TOKEN_STATUS = "/api/v1/token/status/";

    // get all the coin(GET)
    public static String GET_COIN_ALL = "/api/v1/coin/all/";

    // get the currency information that the user has.(POST)
    public static String GET_USER_COIN = "/api/v1/user/own/";


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

        CompletableFuture<List<Ticker>> bidsFuture = CompletableFuture.supplyAsync(() -> {
            List<Ticker> list = null;
            try {
                list = getBids(symbol);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        });

        CompletableFuture<List<Ticker>> asksFuture = CompletableFuture.supplyAsync(() -> {
            List<Ticker> list = null;
            try {
                list = getAsks(symbol);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        });

        try {
            depth.setBids(bidsFuture.get());
            depth.setAsks(asksFuture.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return depth;
    }


    public boolean validateToken() {
        String path = "/api/v1/token/status/";
        String response = sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, GET_TOKEN);
        DragonexApiResponse dragonexApiResponse = null;
        try {
            dragonexApiResponse = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexToken>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dragonexApiResponse.ok;
    }

    public void setToken() throws IOException {
        String response = sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, GET_TOKEN);
        log.info("Get token response:{}", response);
        this.token = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexToken>>() {
        }).checkAndReturn();
        log.info("Set token to http params!");
        HttpParams.setToken(this.token.getToken());
    }

    @Override
    public BalanceDto getBalance(String symbol) {
        String path = "/api/v1/user/own/";
        AtomicReference<BalanceDto> dto = new AtomicReference();
        String response = sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, path);
        log.info(response);
        DragonexBalance dragonexBalance[] = null;
        try {
            dragonexBalance = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexBalance[]>>() {
            }).checkAndReturn();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dragonexBalance != null) {
            Arrays.stream(dragonexBalance).forEach(d -> {
                if (d.getCurrency().equals(symbol)) {
                    dto.set(d.to());
                }
            });
        }
        return dto.get();
    }


    public List<BalanceDto> getBalances() {
        List<BalanceDto> list = new ArrayList<>();
        String path = "/api/v1/user/own/";
        String response = sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, path);
        log.info(response);
        DragonexBalance dragonexBalance[] = null;
        try {
            dragonexBalance = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexBalance[]>>() {
            }).checkAndReturn();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (dragonexBalance != null) {
            Arrays.stream(dragonexBalance).forEach(d -> {
                list.add(d.to());

            });
        }
        return list;
    }

    @Override
    public String placeOrder(OrderRequest request) {
        DragonexOrderRequest dragonexOrderRequest = DragonexOrderRequest.builder().price(String.valueOf(request.getPrice()))
                .symbolId(this.getSymbolId(request.getSymbol())).volume(String.valueOf(request.getAmount())).build();
        String path;
        if (request.getType() == OrderType.BUY_LIMIT) {
            path = "/api/v1/order/buy/";
        } else {
            path = "/api/v1/order/sell/";
        }
        String response = HttpUtils.sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, path, JSONUtil.toJsonString(dragonexOrderRequest));
        DragonexOrderResponse dragonexOrderResponse = null;
        try {
            dragonexOrderResponse = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexOrderResponse>>() {
            }).checkAndReturn();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(dragonexOrderResponse.getOrderId());
    }

    @Override
    public OrderResponse getFilledOrder(String orderId) {
        return this.getFilledOrder(orderId, "ethusdt");
    }

    public OrderResponse getFilledOrder(String orderId, String symbol) {
        String path = "/api/v1/order/detail/";
        DragonexOrderDetailRequest request = DragonexOrderDetailRequest.builder().orderId(Long.valueOf(orderId))
                .symbolId(this.getSymbolId(symbol)).build();
        String response = HttpUtils.sendPost(this.accessKeyId, this.accessKeySecret, MAIN_HOST, path
                , JSONUtil.toJsonString(request));
        DragonexOrderDetailResponse dragonexOrderResponse = null;
        try {
            dragonexOrderResponse = JSONUtil.readValue(response, new TypeReference<DragonexApiResponse<DragonexOrderDetailResponse>>() {
            }).checkAndReturn();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OrderType orderType;
        if (dragonexOrderResponse.getOrderType() == 1) {
            orderType = OrderType.BUY_LIMIT;
        } else {
            orderType = OrderType.SELL_LIMIT;
        }
        OrderState orderState = OrderState.none;
        if (dragonexOrderResponse.getTradeVolume().equals(dragonexOrderResponse.getVolume())) {
            orderState = OrderState.filled;
        }
        return OrderResponse.builder().createTime(new Date(dragonexOrderResponse.getTimestamp()))
                .filledAmount(Double.valueOf(dragonexOrderResponse.getTradeVolume()))
                .price(Double.valueOf(dragonexOrderResponse.getPrice())).symbol(symbol)
                .type(orderType).orderState(orderState).build();
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

    private void initSymbol() {
        if (this.symbolIdCache.isEmpty() || this.symbolCache.isEmpty()) {
            log.info("Init dragonex symbols.....");
            JSONObject jsonObject = super.parseJSONByURL("https://openapi.dragonex.im/api/v1/symbol/all/");
            try {
                DragonexSymbol[] symbols = JSONUtil.readValue(jsonObject.toString(), new TypeReference<DragonexApiResponse<DragonexSymbol[]>>() {
                }).checkAndReturn();
                Arrays.stream(symbols).forEach(x -> {
                    x.setSymbol(x.getSymbol().replace("_", ""));
                    this.symbolCache.put(x.getSymbol(), x);
                    this.symbolIdCache.put(x.getSymbolId(), x);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("Init dragonex symbols.....Done!!");
        }
    }


    // fixme : https://openapi.dragonex.im/api/v1/symbol/all/
    protected int getSymbolId(String symbol) {
        return this.symbolCache.get(symbol.toLowerCase()).getSymbolId();
    }

    protected String getSymbol(int symbolId) {
        return this.symbolIdCache.get(symbolId).getSymbol();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        String json = "{\"room_id\":\"market-quote-multi-buy-coin-103\",\"msg_type\":\"market-quote-multi-buy\",\"coin_id\":103,\"data\":[\"204.3665\",\"7.0135\",\"204.2362\",\"5.1400\",\"204.1089\",\"1.2012\",\"203.9777\",\"6.1930\",\"203.8493\",\"7.1020\",\"203.7190\",\"12.5372\",\"203.5488\",\"4.1800\",\"203.4599\",\"6.0400\",\"203.4198\",\"5.5700\",\"203.2892\",\"0.6200\"],\"timestamp\":1536935268766399097}";
        List<Ticker> list = new ArrayList<>();
        String msgType = "";
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray array = jsonObject.getJSONArray("data");
            msgType = jsonObject.getString("msg_type");
            for (int i = 0; i < array.length() - 1; i++) {
                Ticker ticker = new Ticker();
                ticker.setPrice(Double.valueOf(array.getString(i)));
                ticker.setVolume(Double.valueOf(array.getString(i + 1)));
                list.add(ticker);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        log.info(list.toString());
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
//		send("Hello, it is me. Mario :)");
//		send("{\"cmd\": \"login\", \"value\": \"{\\\"path\\\": \\\"/ws\\\", \\\"headers\\\": {\\\"token\\\": \\\"A8dPdAIWbOZXZC/QVK7PJ/pRmeg=\\\", \\\"Date\\\": \\\"Thu, 17 May 2018 06:17:45 GMT\\\", \\\"Content-Type\\\": \\\"application/json\\\", \\\"Auth\\\": \\\"42be04a2f49e507db56b7ca65a64acac:3A2S8Q/zVCkLGEBdbk8HDY3BolI=\\\"}, \\\"method\\\": \\\"\\\"}\"}");
        send("{\"value\": \"{\\\"roomid\\\": \\\"market-quote-multi-buy-coin-103\\\"}\", \"cmd\": \"sub\"}");
        send("{\"value\": \"{\\\"roomid\\\": \\\"market-quote-multi-sell-coin-103\\\"}\", \"cmd\": \"sub\"}");
        log.info("opened connection");

        // if you plan to refuse connection based on ip or httpfields overload: onWebsocketHandshakeReceivedAsClient
    }

    @Override
    public void onMessage(String message) {
        if (message.indexOf("\"ok\":true,\"code\":1,\"msg\":\"\"") != -1) {
            return;
        }
        List<Ticker> list = new ArrayList<>();
        String msgType = "";
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONArray array = jsonObject.getJSONArray("data");
            msgType = jsonObject.getString("msg_type");
            for (int i = 0; i < array.length() - 1; i++) {
                Ticker ticker = new Ticker();
                ticker.setPrice(Double.valueOf(array.getString(i)));
                ticker.setVolume(Double.valueOf(array.getString(i + 1)));
                list.add(ticker);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (msgType.equals("market-quote-multi-buy")) {
            this.depthHelper.setDragonexBuy(list);
        }
        if (msgType.equals("market-quote-multi-sell")) {
            this.depthHelper.setDragonexSell(list);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // The codecodes are documented in class org.java_websocket.framing.CloseFrame
        log.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.connect();
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
        // if the error is fatal then onClose will be called additionally
    }
}

@Data
class DragonexApiResponse<T> {
    boolean ok;
    int code;
    String msg;
    T data;

    public T checkAndReturn() {
        if (ok) {
            return data;
        }
        throw new ApiException(String.valueOf(code), msg);
    }
}

/**
 * {"ok": true, "code": 1, "data": {"token": "KbtSrikcQKonyZpVwJwMGRJoVhU=", "expire_time": 1529915196}, "msg": ""}
 */
@Data
class DragonexToken {

    String token;

    @JsonProperty("expire_time")
    long expireTime;

    public long getExpireTime() {
        return expireTime * 1000;
    }
}

/**
 * 2018-06-24 18:55:35,545 main | com.jordan.ban.market.parser.Dragonex | {"ok": true, "code": 1, "msg": "",
 * "data": [{"coin_id": 1, "code": "usdt", "frozen": "0.00000000", "volume": "15.45384615"}]}
 */
@Data
class DragonexBalance {
    @JsonProperty("coin_id")
    private int coidId;

    @JsonProperty("code")
    private String currency;

    private double volume;

    private double frozen;

    BalanceDto to() {
        return BalanceDto.builder().available(this.volume - this.frozen)
                .currency(this.currency).frozen(frozen).balance(this.volume).build();
    }
}

@Data
class DragonexSymbol {
    String symbol;
    @JsonProperty("symbol_id")
    int symbolId;
}

@Data
@Builder
class DragonexOrderRequest {
    @JsonProperty("symbol_id")
    private int symbolId;

    private String price;

    private String volume;
}

@Data
class DragonexOrderResponse {
    @JsonProperty("order_id")
    long orderId;
    String price;

    int status;
    long timestamp;
    @JsonProperty("trade_volume")
    String tradeVolume;
    String volume;
}

@Data
@Builder
class DragonexOrderDetailRequest {
    @JsonProperty("symbol_id")
    int symbolId;
    @JsonProperty("order_id")
    long orderId;
}

/**
 * 字段名	数据类型	说明
 * order_id	int	订单ID
 * order_type	int	订单类型：1-买单，2-卖单
 * price	string	下单价格
 * status	int	订单状态
 * symbol_id	int	交易对ID
 * timestamp	int	时间戳
 * trade_volume	string	已成交数量
 * volume	string	下单数量
 */
@Data
class DragonexOrderDetailResponse {
    @JsonProperty("order_id")
    long orderId;
    @JsonProperty("order_type")
    int orderType;
    String price;
    int status;
    @JsonProperty("symbol_id")
    int symbolId;
    long timestamp;
    @JsonProperty("trade_volume")
    String tradeVolume;
    String volume;

}

