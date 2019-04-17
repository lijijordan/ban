package com.jordan.ban.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.RoundingMode;
import java.text.DecimalFormat;

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

    public double getAmount() {
        return round(this.amount);
    }

    public double getPrice(){
        return round2(this.price);
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(df.format(d));
    }

    private static double round2(double d) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);
        return Double.parseDouble(df.format(d));
    }

    public static void main(String[] args) {

        double d = 0.3765620659917748;
        OrderRequest buyOrder = OrderRequest.builder().amount(0.3765620659917748)
                .price(12).symbol("").type(OrderType.BUY_LIMIT).build();
        System.out.println(buyOrder.getAmount());
        System.out.println();
    }
}