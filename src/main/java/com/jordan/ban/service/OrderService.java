package com.jordan.ban.service;

import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.domain.OrderRequest;
import com.jordan.ban.domain.OrderResponse;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public List<Order> getUnfilledOrders() {
        List<Order> orders = this.orderRepository.findAllUnfilledOrders();
        orders.forEach(order -> {
            Hibernate.initialize(order.getPlatform());
        });
        return orders;
    }


    //    @Transactional(Pro=Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
    @Async
    public void refreshOrderState(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        OrderResponse orderResponse = marketParser.getFilledOrder(order.getOrderId());
        if (orderResponse != null) {
            order.setState(orderResponse.getOrderState());
            order.setFillFees(orderResponse.getFillFees());
            order.setFilledAmount(orderResponse.getFilledAmount());
            order.setUpdateTime(new Date());
            order.setFillFees(orderResponse.getFillFees());
            this.orderRepository.save(order);
        }
    }

    /**
     * 创建订单
     *
     * @param orderRequest
     * @param market
     * @param pair
     * @return
     */
    public Order placeOrder(OrderRequest orderRequest, MarketParser market, String pair) {
        Long orderAid = market.placeOrder(orderRequest);
        if (orderAid == null) {
            throw new RuntimeException("创建订单失败！");
        }
        return this.orderRepository.save(Order.builder().price(orderRequest.getPrice()).amount(orderRequest.getAmount())
                .state(OrderState.none).type(orderRequest.getType()).orderPairKey(pair)
                .platform(market.getName())
                .orderId(orderAid).build());
    }
}
