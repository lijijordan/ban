import com.jordan.ban.TacticsApplication;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.MockTradeService;
import com.jordan.ban.service.TradeService;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TacticsApplication.class)
@Slf4j
public class MockTradeServiceTest {

    @Autowired
    private MockTradeService mockTradeService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TradeService tradeService;

    @Before
    public void createAccount() {
//        System.setProperty("http.proxyHost", "localhost");
//        System.setProperty("http.proxyPort", "8001");
//        System.setProperty("https.proxyHost", "localhost");
//        System.setProperty("https.proxyPort", "8001");
//
//        System.setProperty("socksProxyHost", "127.0.0.1");
//        System.setProperty("socksProxyPort", "1080");
        log.info("Clear Account data!");
        this.accountService.emptyAccount();
        this.accountService.initAccount(Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME,
                "LTC_USDT", 14.5632);

    }

    @Test
    public void testTradeAvg() {
        double avg = this.tradeRecordRepository.avgEatDiffPercent(81, 80, "LTC_USDT");
        System.out.println(avg);
    }

    @Test
    public void testTrade1() throws JSONException {
        Assert.assertNotNull(mockTradeService);
        String message = "{\"a2b\":{\"eatDiff\":-0.024919199999999704,\"eatPercent\":-0.0018282880159649962,\"tradeDiff\":-0.0139627999999987,\"tradePercent\":-0.0010236131576824284,\"tradeDirect\":\"A2B\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":347.3841,\"sellCost\":4725.306254567639,\"buyCost\":4733.9422232936395,\"tradeVolume\":213.82,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.0951628000000013,\"eatPercent\":-0.006976386842317571,\"tradeDiff\":-0.08411920000000028,\"tradePercent\":-0.0061717119840350035,\"tradeDirect\":\"B2A\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":213.82,\"sellCost\":2902.157435236,\"buyCost\":2910.8211650519997,\"tradeVolume\":347.3841,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"}}";
        JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
        // TODO: mock mockTrade.
        mockTradeService.mockTrade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
//        tradeService.mockTrade(JSONUtil.getEntity(jsonObject.getString("b2a"),MockTradeResultIndex.class));

    }

    @Test
    public void testTrade2() {
        MarketParser m1 = MarketFactory.getMarket(Huobi.PLATFORM_NAME);
        MarketParser m2 = MarketFactory.getMarket(Fcoin.PLATFORM_NAME);
        String symbol = "LTC_USDT";
        long start = System.currentTimeMillis();
        Depth depth1 = m1.getDepth("eosusdt");
        Depth depth2 = m2.getDepth(symbol);
        double d1ask = depth1.getAsks().get(0).getPrice();
        double d1askVolume = depth1.getAsks().get(0).getVolume();
        double d1bid = depth1.getBids().get(0).getPrice();
        double d1bidVolume = depth1.getBids().get(0).getVolume();
        double d2ask = depth2.getAsks().get(0).getPrice();
        double d2askVolume = depth2.getAsks().get(0).getVolume();
        double d2bid = depth2.getBids().get(0).getPrice();
        double d2bidVolume = depth2.getBids().get(0).getVolume();
        MarketDepth marketDepth = new MarketDepth(d1ask, d1askVolume, d1bid, d1bidVolume, d2ask, d2askVolume, d2bid, d2bidVolume);
//        tradeService.mockTrade(ProductApplication.a2b(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));
//        tradeService.mockTrade(ProductApplication.b2a(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));

    }


    @Test
    public void testTrade3() throws JSONException {
        Assert.assertNotNull(mockTradeService);
        String message = "{\"a2b\":{\"eatDiff\":-0.024919199999999704,\"eatPercent\":-0.0018282880159649962,\"tradeDiff\":-0.0139627999999987,\"tradePercent\":-0.0010236131576824284,\"tradeDirect\":\"A2B\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":347.3841,\"sellCost\":4725.306254567639,\"buyCost\":4733.9422232936395,\"tradeVolume\":213.82,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.0951628000000013,\"eatPercent\":-0.006976386842317571,\"tradeDiff\":-0.08411920000000028,\"tradePercent\":-0.0061717119840350035,\"tradeDirect\":\"B2A\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":213.82,\"sellCost\":2902.157435236,\"buyCost\":2910.8211650519997,\"tradeVolume\":347.3841,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"}}";
        JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
        // TODO: mock mockTrade.
        this.tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
//        tradeService.mockTrade(JSONUtil.getEntity(jsonObject.getString("b2a"),MockTradeResultIndex.class));
    }


    @Test
    public void searchAccount() {
        Assert.assertNotNull(this.accountRepository.findById(1l));
        Assert.assertNotNull(this.accountRepository.findBySymbolAndPlatform("EOSUSDT", "Huobi"));
    }
}
