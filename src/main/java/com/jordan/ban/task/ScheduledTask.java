package com.jordan.ban.task;

import com.jordan.ban.common.Context;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.service.HealthChecker;
import com.jordan.ban.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HealthChecker healthChecker;

    private static final long CHECK_ORDER_RATE = 50000;//50 second
    private static final long CHECK_ORDER_DELAYT = 10000;//10 second

    private static final long HOURS_ONE = 1000 * 60 * 60; // 1 hour


    private static final long THREE_MIN = 1000 * 60 * 3;//3 min

    @Scheduled(initialDelay = CHECK_ORDER_DELAYT, fixedRate = CHECK_ORDER_RATE)
    public void watchUnfilledOrder() {
        List<Order> list = orderService.getUnfilledOrders();
        if (list != null && list.size() > 0) {
            log.info("Waiting for fix order:[{}]" + list.size());
            Context.setUnFilledOrderNum(list.size());
        }
        list.forEach(order -> {
            this.orderService.refreshOrderState(order);
        });
    }


    /**
     * Reset Dragonex token
     */
    @Scheduled(initialDelay = HOURS_ONE, fixedRate = HOURS_ONE)
    public void resetDragonexToken() {
        Dragonex dragonex = (Dragonex) MarketFactory.getMarket(Dragonex.PLATFORM_NAME);
        try {
            dragonex.setToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Scheduled(initialDelay = HOURS_ONE, fixedRate = THREE_MIN)
    public void checkHealth() {
        this.healthChecker.check();
    }


}