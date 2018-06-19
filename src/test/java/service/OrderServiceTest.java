package service;


import com.jordan.ban.BanApplication;
import com.jordan.ban.domain.OrderRequest;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BanApplication.class)
@Slf4j
public class OrderServiceTest {


    @Autowired
    private OrderService orderService;


    @Test
    public void createOrder() {
        OrderRequest request = OrderRequest.builder().amount(0.01).price(0.1)
                .symbol("ltcusdt").type(OrderType.BUY_LIMIT).build();
        orderService.createOrder(request, MarketFactory.getMarket("Huobi"), "");
    }

    @Test
    public void getOrder() {
    }

    @Test
    public void testRefreshOrder() {

        this.orderService.getUnfilledOrders().forEach(order -> {
            log.info("order:{}", order);
            orderService.refreshOrderState(order);
        });
    }


    @Test
    public void createOrderAndRefresh() {
        OrderRequest request = OrderRequest.builder().amount(0.001).price(98.16)
                .symbol("ltcusdt").type(OrderType.BUY_LIMIT).build();
        Order order = orderService.createOrder(request, MarketFactory.getMarket("Huobi"), "");
        for (int i = 0; i < 100; i++) {
            orderService.refreshOrderState(order);
            try {
                Thread.sleep(1000 * 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
