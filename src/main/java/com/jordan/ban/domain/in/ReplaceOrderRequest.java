package com.jordan.ban.domain.in;

import lombok.Data;

@Data
public class ReplaceOrderRequest {

    private double price;
    private String orderId;
}
