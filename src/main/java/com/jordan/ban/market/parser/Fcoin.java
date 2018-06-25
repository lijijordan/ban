package com.jordan.ban.market.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordan.ban.domain.*;
import com.jordan.ban.exception.ApiException;
import com.jordan.ban.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
public class Fcoin extends BaseMarket implements MarketParser {

    private static final String MAC_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";
    public static final String PLATFORM_NAME = "Fcoin";

    private static final String API_HOST = "https://api.fcoin.com/v2/";

    private ConcurrentHashMap<String, BalanceDto> balanceCache;

    private static String DEPTH_URL_TEMPLATE = "https://api.fcoin.com/v2/market/depth/L20/%s";

    String accessKeyId;
    String accessKeySecret;

    public Fcoin(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.balanceCache = new ConcurrentHashMap();
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
        symbol = symbol.toLowerCase();
        JSONObject jsonObject = null;
        try {
            jsonObject = this.parseJSONByURL(String.format(DEPTH_URL_TEMPLATE, symbol)).getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Depth depth = new Depth();
        List<Ticker> bidList = new ArrayList();
        List<Ticker> askList = new ArrayList();
        try {
            long time = System.currentTimeMillis();
            JSONArray bids = jsonObject.getJSONArray("bids");
            JSONArray asks = jsonObject.getJSONArray("asks");
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

    @Override
    public BalanceDto getBalance(String symbol) {
        if (this.balanceCache.get(symbol) != null) {
            return this.balanceCache.get(symbol);
        }
        for (BalanceDto balanceDto : this.getBalances()) {
            if (balanceDto.getCurrency().equals(symbol.toLowerCase())) {
                this.balanceCache.put(symbol, balanceDto);
                return balanceDto;
            }
        }
        return null;
    }

    @Override
    public String placeOrder(OrderRequest orderRequest) {
        log.info("【Fcoin】place order:" + orderRequest.toString());
        FcoinOrderRequest request = new FcoinOrderRequest();
        request.setAmount(orderRequest.getAmount());
        request.setPrice(orderRequest.getPrice());
        if (orderRequest.getType() == OrderType.SELL_LIMIT
                || orderRequest.getType() == OrderType.SELL_MARKET) {
            request.setSide("sell");
        } else {
            request.setSide("buy");
        }
        request.setSymbol(orderRequest.getSymbol());
        request.setType("limit");
        return this.placeOrder(request);
    }

    @Override
    public OrderResponse getFilledOrder(String orderId) {
        /*FcoinMatchresultsOrdersDetailResponse response = this.matchresults(String.valueOf(orderId));
        if (response != null) {
            OrderType orderType;
            if (response.getSide().equals("buy")) {
                orderType = OrderType.BUY_LIMIT;
            } else {
                orderType = OrderType.SELL_LIMIT;
            }
            return OrderResponse.builder().createTime(new Date(response.getCreated_at()))
                    .filledAmount(response.getFilled_amount()).fillFees(response.getFill_fees()).type(orderType).build();
        }
        return null;*/

        FcoinOrdersDetailResponse response = this.getOrdersDetail(String.valueOf(orderId));
        if (response != null) {
            OrderType orderType;
            if (response.getSide().equals("buy")) {
                orderType = OrderType.BUY_LIMIT;
            } else {
                orderType = OrderType.SELL_LIMIT;
            }
            return OrderResponse.builder().createTime(response.getCreated_at()).orderState(response.getState()).price(response.getPrice()).symbol(response.getSymbol())
                    .filledAmount(response.getFilled_amount()).fillFees(response.getFill_fees()).type(orderType).build();
        }
        return null;
    }

    @Override
    public boolean cancelOrder(String orderId) {
        return false;
    }


    private void parseOrder(List<Ticker> tickers, JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i = i + 2) {
            double price = jsonArray.getDouble(i);
            double size = jsonArray.getDouble(i + 1);
            Ticker order1 = new Ticker();
            order1.setPrice(price);
            order1.setVolume(size);
            tickers.add(order1);
        }
    }


    public List<BalanceDto> getBalances() {
        List<BalanceDto> list = new ArrayList<>();
        FcoinApiResponse<FcoinBalance[]> resp = get(API_HOST + "accounts/balance",
                null, new TypeReference<FcoinApiResponse<FcoinBalance[]>>() {
                });
        Arrays.stream(resp.checkAndReturn()).forEach(fcoinBalance -> {
            list.add(BalanceDto.builder().available(fcoinBalance.available).balance(fcoinBalance.balance)
                    .currency(fcoinBalance.currency).frozen(fcoinBalance.frozen).build());
        });
        return list;
    }

    /**
     * HTTP Request
     * GET https://api.fcoin.com/v2/orders/{order_id} 此 API 用于返回指定的订单详情。
     *
     * @param orderId
     * @return
     */
    public FcoinOrdersDetailResponse getOrdersDetail(String orderId) {
        FcoinApiResponse<FcoinOrdersDetailResponse> resp = get(API_HOST + "orders/" + orderId,
                null, new TypeReference<FcoinApiResponse<FcoinOrdersDetailResponse>>() {
                });
        return resp.getData();
    }

    /**
     * GET https://api.fcoin.com/v2/orders/{order_id}/match-results 查询某个订单的成交明细
     *
     * @param orderId
     * @return
     */
    public FcoinMatchresultsOrdersDetailResponse matchresults(String orderId) {
        FcoinApiResponse<FcoinMatchresultsOrdersDetailResponse> resp = get(API_HOST + "orders/" + orderId + "match-results",
                null, new TypeReference<FcoinApiResponse<FcoinMatchresultsOrdersDetailResponse>>() {
                });
        return resp.checkAndReturn();
    }


    /**
     * 创建订单
     *
     * @param request CreateOrderRequest object.
     * @return Order id.
     */
    public String placeOrder(FcoinOrderRequest request) {
        FcoinApiResponse<String> resp =
                post(API_HOST + "orders", request, new TypeReference<FcoinApiResponse<String>>() {
                });
        return resp.checkAndReturn();
    }


    public Long getServerTime() {
        return get(API_HOST + "public/server-time", null, new TypeReference<FcoinApiResponse<Long>>() {
        }).checkAndReturn();
    }

    // send a GET request.
    <T> T get(String uri, Map<String, String> params, TypeReference<T> ref) {
        if (params == null) {
            params = new HashMap<>();
        }
        return call("GET", uri, null, params, ref);
    }

    // send a POST request.
    <T> T post(String uri, Object object, TypeReference<T> ref) {
        return call("POST", uri, object, new HashMap<>(), ref);
    }

    // call api by endpoint.
    <T> T call(String method, String uri, Object obj, Map<String, String> params,
               TypeReference<T> ref) {
        try {
            this.createSignature(method, uri, obj, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Request.Builder builder;
            if ("POST".equals(method)) {
                RequestBody body = RequestBody.create(JSON, JSONUtil.writeValue(obj));
                builder = new Request.Builder().url(uri).post(body);
            } else {
                builder = new Request.Builder().url(uri).get();
            }
            builder.addHeader("FC-ACCESS-KEY", this.accessKeyId);
            builder.addHeader("FC-ACCESS-SIGNATURE", params.get("FC-ACCESS-SIGNATURE"));
            builder.addHeader("FC-ACCESS-TIMESTAMP", params.get("FC-ACCESS-TIMESTAMP"));
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            log.info("response:" + s);
            if (ref == null) {
                return null;
            }
            return JSONUtil.readValue(s, ref);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    // Encode as "a=1&b=%20&c=&d=AAA"
    String toQueryString(Map<String, String> params) {
        return String.join("&", params.entrySet().stream().map((entry) -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.toList()));
    }

    private void createSignature(String method, String uri, Object request, Map<String, String> params) throws Exception {
        long timestamp = System.currentTimeMillis();
        String HTTP_METHOD = method;
        String HTTP_REQUEST_URI = uri;
        String TIMESTAMP = String.valueOf(timestamp);
        String POST_BODY = "";
        if (method.equals("POST") && request != null) {
            // init POST_BODY;
            POST_BODY = request.toString();
        }
        String signatureString = HTTP_METHOD + HTTP_REQUEST_URI + TIMESTAMP + POST_BODY;
        log.info("signatureString:" + signatureString);
        signatureString = Base64.getEncoder().encodeToString(signatureString.getBytes(ENCODING));
        log.info("signature string base64:" + signatureString);
        String actualSign = Base64.getEncoder().encodeToString(hmacSHA1Encrypt(signatureString, this.accessKeySecret));
        log.info("actualSign:" + actualSign);
        params.put("FC-ACCESS-KEY", this.accessKeyId);
        params.put("FC-ACCESS-SIGNATURE", actualSign);
        params.put("FC-ACCESS-TIMESTAMP", String.valueOf(timestamp));
    }


    public static byte[] hmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(ENCODING);
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes(ENCODING);
        //完成 Mac 操作
        return mac.doFinal(text);
    }


    public Fcoin() {
    }

    public static void main(String[] args) {
        Fcoin fcoin = (Fcoin) MarketFactory.getMarket("Fcoin");

        /*List<BalanceDto> list = fcoin.getBalances();
        System.out.println(list.toString());*/

//        fcoin.matchresults("12");

//        fcoin.getOrdersDetail("1");


        /*FcoinOrderRequest request = new FcoinOrderRequest();
        request.setAmount(0.1);
        request.setPrice(0.01);
        request.setSide("buy");
        request.setSymbol("ltcusdt");
        request.setType("limit");
        fcoin.placeOrder(request);*/

        /*OrderRequest request = new OrderRequest();
        request.setAmount(0.01);
        request.setPrice(94.55);
        request.setType(OrderType.BUY_LIMIT);
        request.setSymbol("ltcusdt");
        fcoin.placeOrder(request);*/

        FcoinOrdersDetailResponse response = fcoin.getOrdersDetail("zM_cP9WUmovRYP20wZdewkUWDooaSaBztE7dm0jkbc8=");
        response.getAmount();
    }
}

/**
 * 参数	默认值	描述
 * symbol	无	交易对
 * side	无	交易方向
 * type	无	订单类型
 * price	无	价格
 * amount	无	下单量
 */
@Data
class FcoinOrderRequest {

    String symbol;
    String side;
    String type;
    double price;
    double amount;

    @Override
    public String toString() {
        ObjectMapper oMapper = new ObjectMapper();
        TreeMap<String, Object> params = oMapper.convertValue(this, TreeMap.class);
        return String.join("&", params.entrySet().stream().map((entry)
                -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.toList()));
    }
}

@Data
class FcoinApiResponse<T> {
    String status;
    String msg;
    T data;

    public T checkAndReturn() {
        if ("0".equals(status)) {
            return data;
        }
        throw new ApiException(status, msg);
    }
}

/**
 * "currency":"zil",
 * "available":"0.000000000000000000",
 * "frozen":"0.000000000000000000",
 * "balance":"0.000000000000000000"
 */
@Data
class FcoinBalance {
    String currency;
    double available;
    double frozen;
    double balance;
}

/**
 * "price": "string",
 * "fill_fees": "string",
 * "filled_amount": "string",
 * "side": "buy",
 * "type": "limit",
 * "created_at": 0
 */
@Data
class FcoinMatchresultsOrdersDetailResponse {
    double price;
    double fill_fees;
    double filled_amount;
    String side;
    String type;
    long created_at;
}

/**
 * {
 * "status": 0,
 * "data": {
 * "id": "9d17a03b852e48c0b3920c7412867623",
 * "symbol": "string",
 * "type": "limit",
 * "side": "buy",
 * "price": "string",
 * "amount": "string",
 * "state": "submitted",
 * "executed_value": "string",
 * "fill_fees": "string",
 * "filled_amount": "string",
 * "created_at": 0,
 * "source": "web"
 * }
 * }
 */
@Data
class FcoinOrdersDetailResponse {
    String id;
    String symbol;
    String type;
    String side;
    double price;
    double amount;
    /**
     * submitted	已提交
     * partial_filled	部分成交
     * partial_canceled	部分成交已撤销
     * filled	完全成交
     * canceled	已撤销
     * pending_cancel	撤销已提交
     */
    OrderState state;
    String executed_value;
    double fill_fees;
    double filled_amount;
    Date created_at;
    String source;
}