package com.jordan.ban.entity;


import com.jordan.ban.domain.CycleType;
import com.jordan.ban.domain.TradeDirect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfitStatistics extends BaseEntity {

    private String symbol;

    private double sumMoney;

    private double sumCoin;

    private CycleType cycleType;

    private String platformA;

    private String platformB;


    /**
     * 总的交易金额
     */
    private double totalCostMoney;
    /**
     * increase money
     */
    private double increase;

    /**
     * increase money percent
     * increasePercent = increase/sumMoney
     */
    private double increasePercent;

    private double sumCostMoney;

    private double sumProfit;

    private double avgA2BDiffPercent;

    private double avgB2ADiffPercent;

    private double sumA2BProfit;

    private double sumB2AProfit;

    private double avgA2BProfit;
    private double avgB2AProfit;

}
