import com.jordan.ban.BanApplication;
import com.jordan.ban.ProductApplication;
import com.jordan.ban.common.Constant;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.TradeService;
import com.jordan.ban.utils.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BanApplication.class)
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
        this.accountService.initAccount(ProductApplication.huobi, ProductApplication.gateio,
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
        String message = "{\"a2b\":{\"eatDiff\":0.09144399999999914,\"eatPercent\":0.006355132392799996,\"tradeDiff\":0.1130000000000003,\"tradePercent\":0.007847222222222243,\"tradeDirect\":\"A2B\",\"createTime\":1528426966875,\"costTime\":1359,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":35.103001,\"sellCost\":504.08688722622196,\"buyCost\":498.86700077152,\"tradeVolume\":97.58976127,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.22820000000000032,\"eatPercent\":-0.015847222222222245,\"tradeDiff\":-0.20655599999999913,\"tradePercent\":-0.014355132392799995,\"tradeDirect\":\"B2A\",\"createTime\":1528426966875,\"costTime\":1359,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":97.58976127,\"sellCost\":1385.8664615173072,\"buyCost\":1402.481977163424,\"tradeVolume\":35.103001,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"}}";
        JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
        // TODO: mock trade.
        tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
//        tradeService.trade(JSONUtil.getEntity(jsonObject.getString("b2a"),MockTradeResultIndex.class));

    }

    @Test
    public void searchAccount() {
        Assert.assertNotNull(this.accountRepository.findById(1l));
        Assert.assertNotNull(this.accountRepository.findBySymbolAndPlatform("EOSUSDT", "Huobi"));
    }
}
