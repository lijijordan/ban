package com.jordan.ban.es;

import com.jordan.ban.common.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jordan.ban.common.Constant.MOCK_TRADE_INDEX;

/**
 * Created with IntelliJ IDEA.
 * User: liji
 * Date: 16/12/7
 * Time: 上午10:57
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
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
    public static void initClient(String host) throws UnknownHostException {
        client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName(host), 9300));

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


    public static void main(String[] args) throws IOException, ParseException {
        initClient("localhost");

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date start = format.parse("2018/06/28 00:00:00");
        Date end = format.parse("2018/06/28 00:05:00");

        //多条件设置
        MatchPhraseQueryBuilder mpq1 = QueryBuilders.matchPhraseQuery("diffPlatform", "Dragonex-Fcoin");
        MatchPhraseQueryBuilder mpq2 = QueryBuilders.matchPhraseQuery("symbol", "ETHUSDT");
        QueryBuilder qb2 = QueryBuilders.boolQuery()
                .must(mpq1)
                .must(mpq2);
        //System.out.println(sourceBuilder.toString());

        SearchResponse scrollResp = client.prepareSearch(MOCK_TRADE_INDEX)
                .addSort("createTime", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setPostFilter(QueryBuilders.rangeQuery("createTime").from(start.getTime()).to(end.getTime()))
                .setQuery(qb2)
                .setSize(1000).get(); //max of 100 hits will be returned for each scroll
        int i = 0, sum = 0;
        //Scroll until no hits are returned
        do {
            log.info("Total={}, i={}", scrollResp.getHits().getTotalHits(), i++);
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                System.out.println(map.get("symbol"));
                System.out.println((String) map.get("diffPlatform"));
                System.out.println(new Date((long) map.get("createTime")));
                System.out.println("::::::::::::::::::::::::sum:::::::::::::" + sum++);
                if ((map.get("symbol")).equals("ETHUSDT")) {
                }

            }
            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.

    }
}
