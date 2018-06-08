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
import lombok.extern.slf4j.Slf4j;
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
        log.info("Clear Account data!");
        this.accountService.emptyAccount();
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
        String message = "{\"a2b\":{\"eatDiff\":-0.024919199999999704,\"eatPercent\":-0.0018282880159649962,\"tradeDiff\":-0.0139627999999987,\"tradePercent\":-0.0010236131576824284,\"tradeDirect\":\"A2B\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":347.3841,\"sellCost\":4725.306254567639,\"buyCost\":4733.9422232936395,\"tradeVolume\":213.82,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.0951628000000013,\"eatPercent\":-0.006976386842317571,\"tradeDiff\":-0.08411920000000028,\"tradePercent\":-0.0061717119840350035,\"tradeDirect\":\"B2A\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"EOS_USDT\",\"diffPlatform\":\"Gateio-Huobi\",\"eatTradeVolume\":213.82,\"sellCost\":2902.157435236,\"buyCost\":2910.8211650519997,\"tradeVolume\":347.3841,\"platformA\":\"Gateio\",\"platformB\":\"Huobi\"}}";
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
