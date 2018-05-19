package com.jordan.ban.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.utils.JSONUtil;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

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
     * @param hostName the host name
     * @param port     the port
     * @throws UnknownHostException the unknown host exception
     */
    public static void initClient(String hostName, int port) throws UnknownHostException {
        if (client == null) {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), port));

        }
    }

    /**
     * Index.
     */
    @Async(value = "indexExecutor")
    public static void index(Object data, String index, String type) {
        logger.info("************************* index {} *************************", index);
        try {
            ObjectMapper mapper = new ObjectMapper();
            IndexResponse response = client.prepareIndex(index, type)
                    .setSource(mapper.writeValueAsBytes(data)).get();
        } catch (Exception e) {
            logger.error("Index DeviceEventLog Error: {}", e.toString());
            e.printStackTrace();
        }
    }

    public static void indexRestApi(String data, String index, String type) throws IOException {
        HttpPost httpPost = new HttpPost(String.format("http://206.189.183.68:9223/%s/data", index));
        httpPost.setHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity(data);
        httpPost.setEntity(params);
        closeableHttpClient.execute(httpPost);
        /*CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
        HttpEntity entity = response.getEntity();
        String body = EntityUtils.toString(entity, "UTF-8");
        System.out.println(body);*/
    }


    /**
     * index document
     *
     * @param index
     * @param type
     * @param json
     */
//    @Async(value = "indexExecutor")
    public static void index(String index, String type, String json) {
        IndexResponse response = client.prepareIndex(index, type)
                .setSource(json).get();
    }

    @Async(value = "indexExecutor")
    public static void index(String index, String type, String json, String id) {
        IndexResponse response = client.prepareIndex(index, type, id)
                .setSource(json).get();
    }

    public static void main(String[] args) throws IOException {
        Differ differ = new Differ();
        differ.setCreateTime(new Date());
        differ.setDiffer(0.21f);
        differ.setPercentDiffer("21");
        differ.setSymbol("fff");
//        initClient("206.189.183.68", 9323);
        System.out.println(JSONUtil.toJsonString(differ));
        indexRestApi(JSONUtil.toJsonString(differ), Constant.INDEX_NAME, null);
//        index(Constant.INDEX_NAME, "data", JSONUtil.toJsonString(differ));
    }
}
