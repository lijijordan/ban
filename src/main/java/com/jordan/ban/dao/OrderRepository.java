package com.jordan.ban.dao;

import com.jordan.ban.domain.OrderState;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface OrderRepository extends CrudRepository<Order, Long> {

    @Query(value = "SELECT * FROM trade_order WHERE state <> 3", nativeQuery = true)
    List<Order> findAllUnfilledOrders();


    Order findByOrderId(String orderId);

    List<Order> findAllByOrderPairKey(String pairKey);

    List<Order> findAllBySymbolOrderByCreateTimeAsc(String symbol);


    List<Order> findAllByOrderByCreateTime();

    @Query(value = "select * from trade_order where create_time>=? order by create_time desc", nativeQuery = true)
    List<Order> findAllByCreateTime(Date createTime);

//    @Query(value = "select count(u) from Order u where u.state=3 and u.type=?1 and u.updateTime > ?2 and u.updateTime < ?3")
//    long countFilledOrderByType(OrderType type, Date start, Date end);

    long countByTypeAndStateAndUpdateTimeIsBetween(OrderType orderType, OrderState state, Date start, Date end);


}
