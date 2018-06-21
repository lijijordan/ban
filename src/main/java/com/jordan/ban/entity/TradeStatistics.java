package com.jordan.ban.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * 记录每次交易对的收益
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeStatistics extends BaseEntity {

    private String symbol;

    private String buyMarket;

    private String sellMarket;

    private double buyVolume;

    private double sellVolume;

    private double buyPrice;

    private double sellPrice;

    // 购买手续费
    private double buyFees;
    // 卖出的手续费
    private double sellFees;

    private String buyOrderId;

    private String sellOrderId;

    private String orderPairKey;

    private double totalMoney;

    private double totalCoin;

    private double moneyDiff;

    private double coinDiff;
    /**
     * money 差值
     */
    private double moneyDiffPercent;

    private double coinDiffPercent;
}
