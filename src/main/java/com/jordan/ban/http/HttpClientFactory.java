package com.jordan.ban.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;

public class HttpClientFactory {

    private static CloseableHttpClient client = null;

    private static final int RETRY_COUNT = 10;

    public static HttpClient getHttpClient() {
        if (client == null) {
            client = HttpClients.custom().
                    setRetryHandler(new DefaultHttpRequestRetryHandler(RETRY_COUNT, true)).build();
            //超时重新请求次数
        }
        return client;
    }
}
