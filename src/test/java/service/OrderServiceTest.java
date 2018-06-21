package service;


import com.jordan.ban.TacticsApplication;
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
@SpringBootTest(classes = TacticsApplication.class)
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
    public void testRefreshOrderById(){
        Order order = this.orderService.findByOrderId("4a17pXp7BtvogAyDtrT3OvBeVoI5N-ejJXWau3cq4Rs=");
        this.orderService.refreshOrderState(order);
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

    @Test
    public void statisticTrade(){
        orderService.statisticTrade(Order.builder().orderPairKey("43e66392-2827-4223-82a3-0b05fe48662c").build());
        orderService.statisticTrade(Order.builder().orderPairKey("811bec20-0655-4107-8dfb-6ddeaa66811c").build());
    }
}
