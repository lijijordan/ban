package trade;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.OrderRequest;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.service.TradeServiceETH;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class TradeServiceETHTest {


    @Autowired
    private TradeServiceETH tradeService;

    private Fcoin fcoin;

    @Before
    public void init() {
        fcoin = (Fcoin) MarketFactory.getMarket(Fcoin.PLATFORM_NAME);
    }

    @Test
    public void testTrade() throws JSONException {
        String message = "{\"a2b\":{\"eatDiff\":-0.024919199999999704,\"eatPercent\":-0.0018282880159649962,\"tradeDiff\":-0.0139627999999987,\"tradePercent\":-0.0010236131576824284,\"tradeDirect\":\"A2B\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":347.3841,\"sellCost\":4725.306254567639,\"buyCost\":4733.9422232936395,\"tradeVolume\":213.82,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"},\"b2a\":{\"eatDiff\":-0.0951628000000013,\"eatPercent\":-0.006976386842317571,\"tradeDiff\":-0.08411920000000028,\"tradePercent\":-0.0061717119840350035,\"tradeDirect\":\"B2A\",\"createTime\":1528454220732,\"costTime\":994,\"symbol\":\"LTC_USDT\",\"diffPlatform\":\"Fcoin-Huobi\",\"eatTradeVolume\":213.82,\"sellCost\":2902.157435236,\"buyCost\":2910.8211650519997,\"tradeVolume\":347.3841,\"platformA\":\"Fcoin\",\"platformB\":\"Huobi\"}}";
        JSONObject jsonObject = new JSONObject(message);
//            System.out.println(message);
        // TODO: mock mockTrade.
        this.tradeService.trade(JSONUtil.getEntity(jsonObject.getString("a2b"), MockTradeResultIndex.class));
    }


    @Test
    public void testFcoinPlaceOrder() {
        OrderRequest orderRequest = OrderRequest.builder().amount(0.001).price(20000.01).symbol("ethusdt").type(OrderType.SELL_LIMIT).build();
        fcoin.placeOrder(orderRequest);
    }






}
