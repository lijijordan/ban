package com.jordan.ban.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * [{"id":29553,"order-id":59378,
 * "match-id":59335,"symbol":"ethusdt",
 * "type":"buy-limit","source":"api"
 * ,"price":"100.1000000000"
 * ,"filled-amount":"9.1155000000"
 * ,"filled-fees":"0.0182310000"
 * ,"created-at":1494901400435}]
 */

/**
 * Fcoin:
 * "price": "string",
 * "fill_fees": "string",
 * "filled_amount": "string",
 * "side": "buy",
 * "type": "limit",
 * "created_at": 0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private OrderType type;
    // 成交数量
    private double filledAmount;
    // 手续费
    private double fillFees;
    private Date createTime;
    private OrderState orderState;
    private double price;
}