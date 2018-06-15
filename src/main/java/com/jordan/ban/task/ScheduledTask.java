package com.jordan.ban.task;

import com.jordan.ban.common.Constant;
import com.jordan.ban.common.Context;
import com.jordan.ban.entity.Order;
import com.jordan.ban.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ScheduledTask {

    @Autowired
    private OrderService orderService;

    private static final long CHECK_ORDER_RATE = 5000;//5 second
    private static final long CHECK_ORDER_DELAYT = 10000;//10 second

    @Scheduled(initialDelay = CHECK_ORDER_DELAYT, fixedRate = CHECK_ORDER_RATE)
    public void watchUnfilledOrder() {
        List<Order> list = orderService.getUnfilledOrders();
        if (list != null && list.size() > 0) {
            log.info("未处理订单：" + list.toString());
            Context.setUnFilledOrderNum(list.size());
        }
        list.forEach(order -> {
            this.orderService.refreshOrderState(order);
        });
    }


}