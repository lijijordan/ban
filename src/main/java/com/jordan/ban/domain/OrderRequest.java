package com.jordan.ban.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    /**
     * 交易对，必填，例如："ethcny"，
     */
    public String symbol;

    /**
     * 当订单类型为buy-limit,sell-limit时，表示订单数量， 当订单类型为buy-market时，表示订单总金额， 当订单类型为sell-market时，表示订单总数量
     */
    public double amount;

    /**
     * 订单价格，仅针对限价单有效，例如："1234.56"
     */
    public double price;

    /**
     * 订单类型，取值范围"buy-market,sell-market,buy-limit,sell-limit"
     */
    public OrderType type;
}