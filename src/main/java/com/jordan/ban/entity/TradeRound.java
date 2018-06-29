package com.jordan.ban.entity;

import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.domain.TradeRoundState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * 一个交易周期：对应4个order
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeRound extends BaseEntity {

    private String orderPairKey1;

    private String orderPairKey2;

    private TradeRoundState tradeRoundState;

    private double totalMoney;

    private double totalCoin;
}
