package com.jordan.ban.http;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientFactory {

    private static CloseableHttpClient client = null;

    public static HttpClient getHttpClient() {
        if (client == null) {
            client = HttpClients.createDefault();
        }
        return client;
    }
}
