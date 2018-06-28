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
     * increase money
     */
    private double increase;

    /**
     * increase money percent
     * increasePercent = increase/sumMoney
     */
    private double increasePercent;

}
