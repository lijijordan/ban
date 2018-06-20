package com.jordan.ban.dao;

import com.jordan.ban.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

    @Query(value = "SELECT * FROM trade_order WHERE state <> 3", nativeQuery = true)
    List<Order> findAllUnfilledOrders();


    Order findByOrderId(String orderId);
}
