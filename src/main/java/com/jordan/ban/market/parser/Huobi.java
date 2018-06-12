package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.Ticker;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.http.HttpClientFactory;
import com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl;
import jdk.nashorn.internal.runtime.URIUtils;
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


/**
 * 签名运算
 * API 请求在通过 Internet 发送的过程中极有可能被篡改。为了确保请求未被更改，我们会要求用户在每个请求中带上签名（行情 API 除外），来校验参数或参数值在传输途中是否发生了更改。
 *
 * 计算签名所需的步骤：
 *
 * 规范要计算签名的请求
 * 因为使用 HMAC 进行签名计算时，使用不同内容计算得到的结果会完全不同。所以在进行签名计算前，请先对请求进行规范化处理。下面以查询某订单详情请求为例进行说明
 *
 * https://api.huobi.pro/v1/order/orders?
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx
 * &SignatureMethod=HmacSHA256
 * &SignatureVersion=2
 * &Timestamp=2017-05-11T15:19:30
 * &order-id=1234567890
 * 请求方法（GET 或 POST），后面添加换行符\n。
 * GET\n
 * 添加小写的访问地址，后面添加换行符\n。
 * api.huobi.pro\n
 * 访问方法的路径，后面添加换行符\n。
 * /v1/order/orders\n
 * 按照ASCII码的顺序对参数名进行排序(使用 UTF-8 编码，且进行了 URI 编码，十六进制字符必须大写，如‘:’会被编码为'%3A'，空格被编码为'%20')。
 * 例如，下面是请求参数的原始顺序，进行过编码后。
 *
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx
 * order-id=1234567890
 * SignatureMethod=HmacSHA256
 * SignatureVersion=2
 * Timestamp=2017-05-11T15%3A19%3A30
 * 这些参数会被排序为：
 *
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx
 * SignatureMethod=HmacSHA256
 * SignatureVersion=2
 * Timestamp=2017-05-11T15%3A19%3A30
 * order-id=1234567890
 * 按照以上顺序，将各参数使用字符’&’连接。
 *
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2017-05-11T15%3A19%3A30&order-id=1234567890
 * 组成最终的要进行签名计算的字符串如下：
 *
 * GET\n
 * api.huobi.pro\n
 * /v1/order/orders\n
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2017-05-11T15%3A19%3A30&order-id=1234567890
 * 计算签名，将以下两个参数传入加密哈希函数：
 * 要进行签名计算的字符串
 * GET\n
 * api.huobi.pro\n
 * /v1/order/orders\n
 * AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2017-05-11T15%3A19%3A30&order-id=1234567890
 * 进行签名的密钥（SecretKey）
 * b0xxxxxx-c6xxxxxx-94xxxxxx-dxxxx
 * 得到签名计算结果并进行 Base64编码
 *
 * 4F65x5A2bLyMWVQj3Aqp+B4w+ivaA7n5Oi2SuYtCJ9o=
 * 将上述值作为参数Signature的取值添加到 API 请求中。 将此参数添加到请求时，必须将该值进行 URI 编码。
 *
 * 最终，发送到服务器的 API 请求应该为：
 *
 * https://api.huobi.pro/v1/order/orders?AccessKeyId=e2xxxxxx-99xxxxxx-84xxxxxx-7xxxx&order-id=1234567890&SignatureMethod=HmacSHA256&SignatureVersion=2&Timestamp=2017-05-11T15%3A19%3A30&Signature=4F65x5A2bLyMWVQj3Aqp%2BB4w%2BivaA7n5Oi2SuYtCJ9o%3D
 */
@Log
public class Huobi extends BaseMarket implements MarketParser {

    public static final String PLATFORM_NAME = "Huobi";

    private static String PRICE_URL_TEMPLATE = "https://api.huobipro.com/market/detail/merged?symbol=%s";
    private static String DEPTH_URL_TEMPLATE = "https://api.huobipro.com/market/depth?symbol=%s&type=step1";

    private static final String ACCOUNTS_URL = "https://api.huobipro.com/v1/account/accounts";

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


    /**
     *
     */
    public void getAccount() {
        String url = "";

        HttpGet httpGet = new HttpGet(ACCOUNTS_URL);
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse) HttpClientFactory.getHttpClient().execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity entity = response.getEntity();
        String body;
        JSONObject jsonObject = null;
        try {
            body = EntityUtils.toString(entity, "UTF-8");
            jsonObject = new JSONObject(body);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        return jsonObject;
    }

    public static void main(String[] args) {
        Huobi huobi = new Huobi();
        System.out.println(huobi.getDepth("EOSUSDT"));

    }
}
