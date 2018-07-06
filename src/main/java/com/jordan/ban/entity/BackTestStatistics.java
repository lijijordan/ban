package com.jordan.ban.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackTestStatistics extends BaseEntity {

    private Date start;

    private Date end;

    private double metricsBackPercent;

    private String symbol;

    private String platformA;

    private String platformB;

    private double sumMoney;

    private double sumCoin;

    // 总计交易金额
    private double totalCostMoney;

    // 总计交易的次数
    private int tradeCount;

    // 大于0的平均利润
    private double avgPositiveProfit;

    // 小于0的平均利润
    private double avgNegativeProfit;


    private double profit;

    private long total;

    private long queueSize;

    private float upPercent;

    private float downPercent;
}
