package service;


import com.jordan.ban.BanApplication;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.service.OrderService;
import com.jordan.ban.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BanApplication.class)
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
}
