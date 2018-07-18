package com.jordan.ban.market.parser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jordan.ban.common.HttpParams;
import com.jordan.ban.common.HttpUtils;
import com.jordan.ban.domain.*;
import com.jordan.ban.exception.ApiException;
import com.jordan.ban.http.HttpClientFactory;
import com.jordan.ban.utils.JSONUtil;
import io.netty.handler.codec.json.JsonObjectDecoder;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleBinaryOperator;

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

    public Dragonex() {
        this.balanceCache = new ConcurrentHashMap();
        this.symbolCache = new ConcurrentHashMap();
        this.symbolIdCache = new ConcurrentHashMap();
        this.initSymbol();
    }

    public Dragonex(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.balanceCache = new ConcurrentHashMap();
        this.symbolCache = new ConcurrentHashMap();
        this.symbolIdCache = new ConcurrentHashMap();
        this.initSymbol();
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
        try {
            depth.setBids(getBids(symbol));
            depth.setAsks(getAsks(symbol));
        } catch (JSONException e) {
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

        Dragonex dragonex = (Dragonex) MarketFactory.getMarket(Dragonex.PLATFORM_NAME);
//        dragonex.validateToken();
//        dragonex.setToken();

        /*OrderRequest orderRequest = OrderRequest.builder().symbol("ethusdt")
                .price(458.3161).amount(0.0025).type(OrderType.BUY_LIMIT).build();
        String orderId = dragonex.placeOrder(orderRequest);*/
//        List<BalanceDto> balanceDtos = dragonex.getBalances();
        System.out.println("done!");

        // get symbol
//        System.out.println(dragonex.getSymbolId("ethusdt"));

//        System.out.println(dragonex.getBalance("usdt"));

        //place order
        /*OrderRequest orderRequest = OrderRequest.builder().symbol("ethusdt")
                .price(458.3161).amount(0.0025).type(OrderType.BUY_LIMIT).build();
        String orderId = dragonex.placeOrder(orderRequest);
        System.out.println("order id:" + orderId);

        System.out.println(dragonex.getFilledOrder(orderId, "ethusdt"));*/


        while (true) {
            long start = System.currentTimeMillis();
            System.out.println(JSONUtil.toJsonString(dragonex.getDepth("ethusdt")));
            System.out.println("cost time:" + (System.currentTimeMillis() - start));
            Thread.sleep(1000);
        }
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