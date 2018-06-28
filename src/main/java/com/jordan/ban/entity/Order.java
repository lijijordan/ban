package com.jordan.ban.entity;

import com.jordan.ban.domain.OrderType;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.domain.TradeDirect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * 属性	类型	含义解释
 * id	String	订单 ID
 * symbol	String	交易对
 * side	String	交易方向（buy, sell）
 * type	String	订单类型（limit，market）
 * price	String	下单价格
 * amount	String	下单数量
 * state	String	订单状态
 * executed_value	String	已成交
 * filled_amount	String	成交量
 * fill_fees	String	手续费
 * created_at	Long	创建时间
 * source	String	来源
 */
@Data
@Entity(name = "trade_order")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order extends BaseEntity {


    private String platform;

    private String symbol;

    private OrderType type;

    //
    private double price;
    // 下单数量
    private double amount;

    /**
     * submitted	已提交
     * partial_filled	部分成交
     * partial_canceled	部分成交已撤销
     * filled	完全成交
     * canceled	已撤销
     * pending_cancel	撤销已提交
     */
    private OrderState state;

    // 已经成交
    private double executedValue;

    // 成交量
    private double filledAmount;

    // 手续费
    private double fillFees;

    private String source;

    /**
     * 平台生成的ID
     */
    private String orderId;

    // 关联的订单
    private String orderPairKey;

    private TradeDirect tradeDirect;

    private double diffPercent;

}
