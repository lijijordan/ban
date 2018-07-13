package service;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.service.OrderService;
import com.jordan.ban.service.TradeService;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private OrderService orderService;

    @Test
    public void queryOrder() {
        Huobi huobi = (Huobi) MarketFactory.getMarket("Huobi");
        huobi.getAccounts();
    }

    @Test
    public void createOrder() {
        Order order = Order.builder().state(OrderState.submitted).amount(2).price(1.2).type(OrderType.SELL_LIMIT).platform("Huobi")
                .orderId("").build();
        orderService.refreshOrderState(order);
    }

    @Test
    public void testTrade() {
        String json = "{\"tradeVolume\":9.8,\"buyPrice\":98.39,\"platformB\":\"Fcoin\",\"symbol\":\"LTCUSDT\",\"buyCost\":71752.00071129,\"platformA\":\"Huobi\",\"eatDiff\":-0.40356000000000514,\"sellCost\":71458.30207982,\"diffPlatform\":\"Huobi-Fcoin\",\"eatTradeVolume\":727.8055,\"sellPrice\":98.38,\"tradeDiff\":-0.35355999999999377,\"money\":\"USDT\",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"A2B\",\"currency\":\"LTC\",\"eatPercent\":-0.0041016363451570805,\"tradePercent\":-0.003593454619371824}";
        this.tradeService.trade(JSONUtil.getEntity(json, MockTradeResultIndex.class));
    }
}
