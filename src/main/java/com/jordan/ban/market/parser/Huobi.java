package com.jordan.ban.market.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.jordan.ban.domain.*;
import com.jordan.ban.exception.ApiException;
import com.jordan.ban.http.HttpClientFactory;

import com.jordan.ban.utils.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class Huobi extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Huobi";
    private static final long DEFAULT_ACCOUNT_ID = 4027685l;

    private static String PRICE_URL_TEMPLATE = "https://api.huobipro.com/market/detail/merged?symbol=%s";
    private static String DEPTH_URL_TEMPLATE = "https://api.huobipro.com/market/depth?symbol=%s&type=step1";

    private static final String ACCOUNTS_URL = "https://api.huobipro.com/v1/account/accounts";
    String accessKeyId;
    String accessKeySecret;
    String assetPassword;


    static final String API_HOST = "api.hadax.com";

    static final String API_URL = "https://" + API_HOST;

    public Huobi(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.assetPassword = null;
    }


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

    @Override
    public BalanceDto getBalance(String symbol) {
        // FIXME use cache
        for (BalanceDto balanceDto : this.getBalances()) {
            if (balanceDto.getCurrency().equals(symbol.toLowerCase())) {
                return balanceDto;
            }
        }
        return null;
    }

    @Override
    public String placeOrder(OrderRequest request) {
        log.info("【Huobi】place order:" + request.toString());
        CreateOrderRequest createOrderReq = new CreateOrderRequest();
        createOrderReq.accountId = String.valueOf(this.getAccountID());
        createOrderReq.amount = String.valueOf(request.getAmount());
        createOrderReq.price = String.valueOf(request.getPrice());
        createOrderReq.symbol = request.getSymbol();
        switch (request.getType()) {
            case SELL_LIMIT:
                createOrderReq.type = CreateOrderRequest.OrderType.SELL_LIMIT;
                break;
            case BUY_MARKET:
                createOrderReq.type = CreateOrderRequest.OrderType.BUY_MARKET;
                break;
            case SELL_MARKET:
                createOrderReq.type = CreateOrderRequest.OrderType.SELL_MARKET;
                break;
            case BUY_LIMIT:
                createOrderReq.type = CreateOrderRequest.OrderType.BUY_LIMIT;
                break;
        }
        createOrderReq.source = "api";
        return this.placeOrder(createOrderReq);
    }

    public List<BalanceDto> getBalances() {
        // Fixme : use cache
        return this.getBalances(this.getAccountID());
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


    /**
     * GET /v1/account/accounts 查询当前用户的所有账户(即account-id)
     *
     * @return
     */
    public AccountsResponse getAccounts() {
        AccountsResponse resp = get("/v1/account/accounts", null, new TypeReference<AccountsResponse<List<Accounts>>>() {
        });
        return resp;
    }

    public Long getAccountID() {
        return DEFAULT_ACCOUNT_ID;
        /*List<Accounts> list = (List<Accounts>) this.getAccounts().getData();
        Accounts account = list.get(0);
        long accountId = account.getId();
        System.out.println("accountId:" + account);
        return accountId;*/
    }

    /**
     * GET /v1/account/accounts/{account-id}/balance 查询指定账户的余额
     *
     * @param accountId
     * @return
     */
    public List<BalanceDto> getBalances(long accountId) {
        List<BalanceDto> list = new ArrayList<>();
        BalanceResponse resp = get("/v1/account/accounts/" + accountId + "/balance", null, new TypeReference<BalanceResponse<Balance>>() {
        });
        Balance b = (Balance) resp.getData();
        List<Map<String, String>> data = (List<Map<String, String>>) (b.getList());
        data.forEach(stringStringMap -> {
            if (stringStringMap.get("type").equals("trade")) {
                list.add(BalanceDto.builder().balance(Double.valueOf(stringStringMap.get("balance"))).available(Double.valueOf(stringStringMap.get("balance")))
                        .currency(stringStringMap.get("currency")).build());
            }
        });
        return list;
    }


    /**
     * 创建订单
     *
     * @param request CreateOrderRequest object.
     * @return Order id.
     */
    public String placeOrder(CreateOrderRequest request) {
        ApiResponse<String> resp =
                post("/v1/order/orders/place", request, new TypeReference<ApiResponse<String>>() {
                });
        return resp.checkAndReturn();
    }

    /**
     * GET /v1/order/orders/{order-id}/matchresults 查询某个订单的成交明细
     *
     * @param orderId
     * @return
     */
    public MatchedOrder matchresults(String orderId) {
        MatchresultsOrdersDetailResponse<MatchedOrder> resp = get("/v1/order/orders/" + orderId + "/matchresults", null, new TypeReference<MatchresultsOrdersDetailResponse<MatchedOrder>>() {
        });
        return resp.getData();
    }


    @Override
    public OrderResponse getFilledOrder(String orderId) {
        OrderDetail response = this.getOrdersDetail(String.valueOf(orderId));
        if (response != null) {
            OrderType orderType;
            if (response.getType().equals("buy-limit")) {
                orderType = OrderType.BUY_LIMIT;
            } else {
                orderType = OrderType.SELL_LIMIT;
            }
            return OrderResponse.builder().createTime(response.getCreatedAt())
                    .price(response.getPrice())
                    .filledAmount(response.getFieldAmount()).orderState(response.getState())
                    .fillFees(response.getField_fees()).type(orderType).build();
        }
        return null;
    }

    /**
     * GET /v1/order/orders/{order-id} 查询某个订单详情
     *
     * @param orderId
     * @return
     */
    public OrderDetail getOrdersDetail(String orderId) {
        OrdersDetailResponse<OrderDetail> resp = get("/v1/order/orders/" + orderId, null, new TypeReference<OrdersDetailResponse<OrderDetail>>() {
        });
        return resp.getData();
    }

    /**
     * POST /v1/order/orders/batchcancel 批量撤销订单
     *
     * @param orderList
     * @return
     */
    public BatchcancelResponse submitcancels(List orderList) {
        BatchcancelResponse resp = post("/v1/order/orders/batchcancel", orderList, new TypeReference<BatchcancelResponse<Batchcancel<List, BatchcancelBean>>>() {
        });
        return resp;
    }

    /**
     * POST /v1/order/orders/{order-id}/submitcancel 申请撤销一个订单请求
     *
     * @param orderId
     * @return
     */
    public SubmitcancelResponse submitcancel(String orderId) {
        SubmitcancelResponse resp = post("/v1/order/orders/" + orderId + "/submitcancel", null, new TypeReference<SubmitcancelResponse>() {
        });
        return resp;
    }

    @Override
    public boolean cancelOrder(String orderId) {
        SubmitcancelResponse response = this.submitcancel(String.valueOf(orderId));
        return response.getStatus().equals("ok");
    }

    public Huobi() {
    }

    public static void main(String[] args) {
        Huobi huobi = (Huobi) MarketFactory.getMarket("Huobi");

        /*OrderDetail orderDetail = huobi.getOrdersDetail("5898349577");
        System.out.println(orderDetail.getType());
        System.out.println(orderDetail.getState());*/
        huobi.cancelOrder("5898349577");


        /*System.out.println(huobi.getAccounts());

        List<Accounts> list = (List<Accounts>) huobi.getAccounts().getData();
        Accounts account = list.get(0);
        long accountId = account.getId();
        System.out.println("accountId:" + account);
        huobi.getBalance(String.valueOf(accountId));*/
/*
        // order
        Long orderId = 123L;
        if (!list.isEmpty()) {
            // create order:
            CreateOrderRequest createOrderReq = new CreateOrderRequest();
            createOrderReq.accountId = String.valueOf(accountId);
            createOrderReq.amount = "0.02";
            createOrderReq.price = "0.1";
            createOrderReq.symbol = "eosusdt";
            createOrderReq.type = CreateOrderRequest.OrderType.BUY_LIMIT;
            createOrderReq.source = "api";

            //------------------------------------------------------ 创建订单  -------------------------------------------------------
            orderId = huobi.createOrder(createOrderReq);
            System.out.println("order id:" + orderId);
            // place order:
            //------------------------------------------------------ 执行订单  -------------------------------------------------------
//            String r = huobi.createOrder(orderId);
        }*/
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
        return call("POST", uri, object, new HashMap<String, String>(), ref);
    }

    // call api by endpoint.
    <T> T call(String method, String uri, Object object, Map<String, String> params,
               TypeReference<T> ref) {
        ApiSignature sign = new ApiSignature();
        sign.createSignature(this.accessKeyId, this.accessKeySecret, method, API_HOST, uri, params);
        try {
            Request.Builder builder;
            if ("POST".equals(method)) {
                RequestBody body = RequestBody.create(JSON, JSONUtil.writeValue(object));
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).post(body);
            } else {
                builder = new Request.Builder().url(API_URL + uri + "?" + toQueryString(params)).get();
            }
            if (this.assetPassword != null) {
                builder.addHeader("AuthData", authData());
            }
            Request request = builder.build();
            Response response = client.newCall(request).execute();
            String s = response.body().string();
            log.info("response:" + s);
            return JSONUtil.readValue(s, ref);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    String authData() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        md.update(this.assetPassword.getBytes(StandardCharsets.UTF_8));
        md.update("hello, moto".getBytes(StandardCharsets.UTF_8));
        Map<String, String> map = new HashMap<>();
        map.put("assetPwd", DatatypeConverter.printHexBinary(md.digest()).toLowerCase());
        try {
            return ApiSignature.urlEncode(JSONUtil.writeValue(map));
        } catch (IOException e) {
            throw new RuntimeException("Get json failed: " + e.getMessage());
        }
    }
}

/**
 * API签名，签名规范：
 * <p>
 * http://docs.aws.amazon.com/zh_cn/general/latest/gr/signature-version-2.html
 *
 * @Date 2018/1/14
 * @Time 16:02
 */
class ApiSignature {

    final Logger log = LoggerFactory.getLogger(getClass());

    static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");
    static final ZoneId ZONE_GMT = ZoneId.of("Z");

    /**
     * 创建一个有效的签名。该方法为客户端调用，将在传入的params中添加AccessKeyId、Timestamp、SignatureVersion、SignatureMethod、Signature参数。
     *
     * @param appKey       AppKeyId.
     * @param appSecretKey AppKeySecret.
     * @param method       请求方法，"GET"或"POST"
     * @param host         请求域名，例如"be.huobi.com"
     * @param uri          请求路径，注意不含?以及后的参数，例如"/v1/api/info"
     * @param params       原始请求参数，以Key-Value存储，注意Value不要编码
     */
    public void createSignature(String appKey, String appSecretKey, String method, String host,
                                String uri, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append(method.toUpperCase()).append('\n') // GET
                .append(host.toLowerCase()).append('\n') // Host
                .append(uri).append('\n'); // /path
        params.remove("Signature");
        params.put("AccessKeyId", appKey);
        params.put("SignatureVersion", "2");
        params.put("SignatureMethod", "HmacSHA256");
        params.put("Timestamp", gmtNow());
        // build signature:
        SortedMap<String, String> map = new TreeMap<>(params);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append('=').append(urlEncode(value)).append('&');
        }
        // remove last '&':
        sb.deleteCharAt(sb.length() - 1);
        // sign:
        Mac hmacSha256 = null;
        try {
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey =
                    new SecretKeySpec(appSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm: " + e.getMessage());
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid key: " + e.getMessage());
        }
        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String actualSign = Base64.getEncoder().encodeToString(hash);
        params.put("Signature", actualSign);
        if (log.isDebugEnabled()) {
            log.debug("Dump parameters:");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                log.debug("  key: " + entry.getKey() + ", value: " + entry.getValue());
            }
        }
    }

    /**
     * 使用标准URL Encode编码。注意和JDK默认的不同，空格被编码为%20而不是+。
     *
     * @param s String字符串
     * @return URL编码后的字符串
     */
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }

    /**
     * Return epoch seconds
     */
    long epochNow() {
        return Instant.now().getEpochSecond();
    }

    String gmtNow() {
        return Instant.ofEpochSecond(epochNow()).atZone(ZONE_GMT).format(DT_FORMAT);
    }
}

@Data
class AccountsResponse<T> {

    /**
     * status : ok
     * data : [{"id":100009,"type":"spot","state":"working","user-id":1000}]
     */

    private String status;
    public String errCode;
    public String errMsg;
    private T data;

    public T checkAndReturn() {
        if ("ok".equals(status)) {
            return data;
        }
        throw new ApiException(errCode, errMsg);
    }

}


@Data
class Accounts {
    /**
     * id : 100009
     * type : spot
     * state : working
     * user-id : 1000
     */
    private int id;
    private String type;
    private String state;
    private int userid;
}

@Data
class BalanceResponse<T> {
    /**
     * status : ok
     * data : {"id":"100009","type":"spot","state":"working","list":[{"currency":"usdt","type":"mockTrade","balance":"500009195917.4362872650"}],"user-id":"1000"}
     */

    private String status;
    public String errCode;
    public String errMsg;
    private T data;
}

@Data
class Balance<T> {
    /**
     * id : 100009
     * type : spot
     * state : working
     * list : [{"currency":"usdt","type":"mockTrade","balance":"500009195917.4362872650"}]
     * user-id : 1000
     */
    private String id;
    private String type;
    private String state;
    private String userid;
    private T list;
}

class CreateOrderRequest {
    public static interface OrderType {
        /**
         * 限价买入
         */
        static final String BUY_LIMIT = "buy-limit";
        /**
         * 限价卖出
         */
        static final String SELL_LIMIT = "sell-limit";
        /**
         * 市价买入
         */
        static final String BUY_MARKET = "buy-market";
        /**
         * 市价卖出
         */
        static final String SELL_MARKET = "sell-market";
    }

    /**
     * 交易对，必填，例如："ethcny"，
     */
    public String symbol;

    /**
     * 账户ID，必填，例如："12345"
     */
    public String accountId;

    /**
     * 当订单类型为buy-limit,sell-limit时，表示订单数量， 当订单类型为buy-market时，表示订单总金额， 当订单类型为sell-market时，表示订单总数量
     */
    public String amount;

    /**
     * 订单价格，仅针对限价单有效，例如："1234.56"
     */
    public String price = "0.0";

    /**
     * 订单类型，取值范围"buy-market,sell-market,buy-limit,sell-limit"
     */
    public String type;

    /**
     * 订单来源，例如："api"
     */
    public String source = "com/huobi/api";
}

class ApiResponse<T> {

    public String status;
    public String errCode;
    public String errMsg;
    public T data;

    public T checkAndReturn() {
        if ("ok".equals(status)) {
            return data;
        }
        throw new ApiException(errCode, errMsg);
    }
}

@Data
class OrdersDetailResponse<T> {
    /**
     * status : ok
     * data : {"id":59378,"symbol":"ethusdt","account-id":100009,"amount":"10.1000000000","price":"100.1000000000","created-at":1494901162595,"type":"buy-limit","field-amount":"10.1000000000","field-cash-amount":"1011.0100000000","field-fees":"0.0202000000","finished-at":1494901400468,"user-id":1000,"source":"api","state":"filled","canceled-at":0,"exchange":"huobi","batch":""}
     */
    private String status;
    public String errCode;
    public String errMsg;
    private T data;
}

@Data
class MatchedOrder {
    /**
     * {"id":29553,"order-id":59378,"match-id":59335,"symbol":"ethusdt","type":"buy-limit","source":"api","price":"100.1000000000",
     * "filled-amount":"9.1155000000","filled-fees":"0.0182310000","created-at":1494901400435}
     */
    long id;
    @JsonProperty("order-id")
    long orderId;
    @JsonProperty("match-id")
    long matchId;
    String symbol;
    String type;
    String source;
    double price;
    @JsonProperty("filled-amount")
    double filledAmount;
    @JsonProperty("filled-fees")
    double filledFees;
    @JsonProperty("create-at")
    Date createTime;

}

@Data
class MatchresultsOrdersDetailResponse<T> {
    /**
     * status : ok
     * data : [{"id":29553,"order-id":59378,"match-id":59335,"symbol":"ethusdt","type":"buy-limit","source":"api","price":"100.1000000000","filled-amount":"9.1155000000","filled-fees":"0.0182310000","created-at":1494901400435}]
     */
    private String status;
    public String errCode;
    public String errMsg;
    private T data;

}

@Data
class BatchcancelResponse<T> {
    /**
     * status : ok
     * data : {"success":["1","3"],"failed":[{"err-msg":"记录无效","order-id":"2","err-code":"base-record-invalid"}]}
     */

    private String status;
    public String errCode;
    public String errMsg;
    private T data;

}

@Data
class SubmitcancelResponse {
    /**
     * status : ok
     * data : 59378
     */
    private String status;
    public String errCode;
    public String errMsg;
    private String data;
}


/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 17:53
 */

@Data
class BatchcancelBean {
    /**
     * err-msg : 记录无效
     * order-id : 2
     * err-code : base-record-invalid
     */

    private String errmsg;
    private String orderid;
    private String errcode;

}

@Data
class Batchcancel<T1, T2> {
    private T1 success;
    private T2 failed;
}

/**
 * {"id":5898349577,"symbol":"ltcusdt",
 * "account-id":4027685,
 * "amount":"0.010000000000000000",
 * "price":"0.100000000000000000",
 * "created-at":1529037652546,
 * "type":"buy-limit",
 * "field-amount":"0.0",
 * "field-cash-amount":"0.0",
 * "field-fees":"0.0",
 * "finished-at":0,
 * "source":"api",
 * "state":"submitted",
 * "canceled-at":0}
 */
@Data
class OrderDetail {
    double amount;
    double price;
    @JsonProperty("field-amount")
    double fieldAmount;
    @JsonProperty("field-fees")
    double field_fees;
    @JsonProperty("created-at")
    Date createdAt;
    String type;
    OrderState state;
    @JsonProperty("finished-at")
    long finishedAt;
}