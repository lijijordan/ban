package com.jordan.ban.service;

import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.domain.OrderRequest;
import com.jordan.ban.domain.OrderResponse;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.entity.Order;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SlackService slackService;

    /**
     * 获取为完成交易的订单
     *
     * @return
     */
    public List<Order> getUnfilledOrders() {
        return this.orderRepository.findAllUnfilledOrders();
    }


    //    @Transactional(Pro=Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
//    @Async
    public void refreshOrderState(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        OrderResponse orderResponse = marketParser.getFilledOrder(order.getOrderId());
        if (orderResponse != null) {
            order.setState(orderResponse.getOrderState());
            order.setFillFees(orderResponse.getFillFees());
            order.setFilledAmount(orderResponse.getFilledAmount());
            order.setUpdateTime(new Date());
            order.setSymbol(orderResponse.getSymbol());
            order.setPrice(orderResponse.getPrice());
            order.setFillFees(orderResponse.getFillFees());
            this.orderRepository.save(order);
        }
    }


    @Async
    public void cancelOrder(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        if (marketParser.cancelOrder(order.getOrderId())) {
            order.setState(OrderState.canceled);
            order.setUpdateTime(new Date());
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
    public Order createOrder(OrderRequest orderRequest, MarketParser market, String pair) {
        String orderAid = market.placeOrder(orderRequest);
        if (StringUtils.isEmpty(orderAid)) {
            throw new TradeException("Create Order failed！");
        }
        try {
            slackService.sendMessage("Order", "Place order:" + orderRequest.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // update account;
        accountService.updateBalancesCache(market.getName());
        // record order
        return this.orderRepository.save(Order.builder().price(orderRequest.getPrice()).amount(orderRequest.getAmount())
                .state(OrderState.none).type(orderRequest.getType()).orderPairKey(pair)
                .platform(market.getName())
                .orderId(orderAid).build());
    }

    public Order findByOrderId(String orderId) {
        return this.orderRepository.findByOrderId(orderId);
    }
}
