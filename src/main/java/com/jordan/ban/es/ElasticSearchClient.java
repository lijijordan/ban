package com.jordan.ban.es;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.utils.JSONUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: liji
 * Date: 16/12/7
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
public class ElasticSearchClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
    private static final CloseableHttpClient closeableHttpClient = HttpClients.custom()
            .build();
    private static TransportClient client;

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * Gets client.
     *
     * @return the client
     */
    public static TransportClient getClient() {
        return client;
    }

    /**
     * Init client.
     *
     * @throws UnknownHostException the unknown host exception
     */
    public static void initClient() throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

    }

    public static void index(String json) {
        IndexResponse response = client.prepareIndex(Constant.INDEX_NAME, "data")
                .setSource(json, XContentType.JSON)
                .get();
    }

    public static void index(String json, String name) {
        IndexResponse response = client.prepareIndex(name, "data")
                .setSource(json, XContentType.JSON)
                .get();
//        System.out.println("index :" + json);
//        System.out.println(response.toString());
    }

    public static void indexAsynchronous(String json, String name) {
        executor.execute(() -> index(json, name));
    }


    public static void main(String[] args) throws IOException {
        initClient();
        Differ differ = new Differ();
        differ.setDifferPlatform("plat");
        differ.setDiffer(11);
        differ.setCreateTime(new Date());
        differ.setSymbol("etcbtc");
        index(JSONUtil.toJsonString(differ));
    }
}
