import com.jordan.ban.BackTestApplication;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.market.parser.*;
import com.jordan.ban.service.AccountService;
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

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private AccountService accountService;

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
        this.accountService.initAccount(Huobi.PLATFORM_NAME, Gateio.PLATFORM_NAME,
                "EOS_USDT", 14.5632);

    }

    @Test
    public void testTradeAvg() {
        double avg = this.tradeRecordRepository.avgEatDiffPercent(81, 80, "EOS_USDT");
        System.out.println(avg);
    }

    @Test
    public void testTrade1() throws JSONException {
        Assert.assertNotNull(tradeService);
        String message = "{\"a2b\":{\"eatDiff\":-0.024919199999999704,\"eatPercent\":-0.0018282880159649962,\"tradeDiff\":-0.0139627999999987,\"tradePercent\":-0.0010236131576824284,\"tradeDirect\":\"A2B\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":347.3841,\"sellCost\":4725.306254567639,\"buyCost\":4733.9422232936395,\"tradeVolume\":213.82,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.0951628000000013,\"eatPercent\":-0.006976386842317571,\"tradeDiff\":-0.08411920000000028,\"tradePercent\":-0.0061717119840350035,\"tradeDirect\":\"B2A\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":213.82,\"sellCost\":2902.157435236,\"buyCost\":2910.8211650519997,\"tradeVolume\":347.3841,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"}}";
        JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
        // TODO: mock trade.
        tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
//        tradeService.trade(JSONUtil.getEntity(jsonObject.getString("b2a"),MockTradeResultIndex.class));

    }

    @Test
    public void testTrade2() {
        MarketParser m1 = MarketFactory.getMarket(Huobi.PLATFORM_NAME);
        MarketParser m2 = MarketFactory.getMarket(Gateio.PLATFORM_NAME);
        String symbol = "EOS_USDT";
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
//        tradeService.trade(ProductApplication.a2b(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));
//        tradeService.trade(ProductApplication.b2a(marketDepth, depth1, depth2, (System.currentTimeMillis() - start), System.currentTimeMillis()));

    }

    @Test
    public void searchAccount() {
        Assert.assertNotNull(this.accountRepository.findById(1l));
        Assert.assertNotNull(this.accountRepository.findBySymbolAndPlatform("EOSUSDT", "Huobi"));
    }

    @Test
    public void testDragonexAccount() {
        MarketParser marketA = MarketFactory.getMarket(Dragonex.PLATFORM_NAME);
        String coinName = "eth";
        String symbol = "ethusdt";
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(Dragonex.PLATFORM_NAME);
        AccountDto accountA = AccountDto.builder().money(balanceA.get("usdt").getBalance()).platform(marketA.getName()).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getBalance() : 0).build();
        Assert.assertNotNull(accountA);
    }
}
