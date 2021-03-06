package com.jordan.ban.es;

import com.jordan.ban.common.Constant;
import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.out.ESQueryResponse;
import com.jordan.ban.utils.JSONUtil;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.LoggerFactory;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jordan.ban.common.Constant.ETH_USDT;
import static com.jordan.ban.common.Constant.MOCK_TRADE_INDEX;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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

    public static ESQueryResponse query(String platform, String symbol, Date start, Date end, int from, int to) {
        logger.info("ES query . start:{}, end:{}, from:{}, to:{}", start, end, from, to);
        List result = new ArrayList();

        SearchResponse response = client.prepareSearch(MOCK_TRADE_INDEX)
                .setQuery(QueryBuilders.matchPhraseQuery("diffPlatform", "Dragonex-Fcoin"))
//                .setPostFilter(QueryBuilders.matchPhraseQuery("symbol", ETH_USDT))
//                .setQuery(QueryBuilders.matchQuery("symbol", ETH_USDT))
//                .setPostFilter( QueryBuilders.boolQuery()
//                        .must(QueryBuilders.matchPhraseQuery("symbol", ETH_USDT))
//                        .must(QueryBuilders.matchPhraseQuery("diffPlatform", "Dragonex-Fcoin"))
                .setPostFilter(QueryBuilders.rangeQuery("createTime").from(start.getTime()).to(end.getTime()))
                .setFrom(from).setSize(to).setExplain(true)
                .addSort(SortBuilders.fieldSort("createTime"))
                .get();
        System.out.println(response.getHits().getTotalHits());
        Arrays.stream(response.getHits().getHits()).forEach(h -> {
            result.add(h.getSourceAsMap());
        });
        return ESQueryResponse.builder().result(result).total(response.getHits().totalHits).build();
    }


    public static void indexAsynchronous(String json, String name) {
        executor.execute(() -> index(json, name));
    }


    public static void main(String[] args) throws IOException, ParseException {
        initClient();

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date start = format.parse("2018/06/28 00:00:00");
        Date end = format.parse("2018/06/28 00:05:00");

        /*Differ differ = new Differ();
        differ.setDifferPlatform("plat");
        differ.setDiffer(11);
        differ.setCreateTime(new Date());
        differ.setSymbol("etcbtc");
        index(JSONUtil.toJsonString(differ));*/


        /*long startTime = System.currentTimeMillis() - (1000l * 60 * 60 * 24); // one day before
        SearchResponse response = client.prepareSearch("mock_trade")
                .setQuery(QueryBuilders.matchQuery("diffPlatform", "Dragonex-Fcoin"))
                .setQuery(QueryBuilders.matchQuery("symbol", "ETHUSDT"))
//                .setFrom(0).setSize(60).setExplain(true)
                .setPostFilter(QueryBuilders.rangeQuery("createTime").from(new Date(startTime).getTime()).to(new Date().getTime()))
                .get();
        System.out.println(response.getHits().getTotalHits());
        System.out.println(response.getHits().getHits()[0].getSourceAsMap());
        System.out.println(new Date((long) response.getHits().getHits()[0].getSourceAsMap().get("createTime")));
        System.out.println(new Date((long) response.getHits().getHits()[1].getSourceAsMap().get("createTime")));
*/


        /*
        System.out.println(start);

        List<Map<String, Object>> list = query("Dragonex-Fcoin", "ETHUSDT", start, end, 0, 200).getResult();
        System.out.println(list.size());
        list.stream().forEach(map -> {

            System.out.println((String) map.get("diffPlatform"));
            System.out.println((String) map.get("symbol"));
        });*/

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
