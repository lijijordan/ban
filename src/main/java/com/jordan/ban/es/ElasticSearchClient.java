package com.jordan.ban.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.utils.JSONUtil;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;

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

    public static void main(String[] args) throws UnknownHostException {
        Differ differ = new Differ();
        differ.setCreateTime(new Date());
        differ.setDiffer(0.21f);
        differ.setPercentDiffer("21");
        differ.setSymbol("fff");
        initClient("localhost", 9300);
        index(Constant.INDEX_NAME, "data", JSONUtil.toJsonString(differ));
    }
}
