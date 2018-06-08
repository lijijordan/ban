package com.jordan.ban.entity;

import com.jordan.ban.domain.TradeDirect;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

@Data
@Entity
public class TradeRecord extends BaseEntity {

    private Date tradeTime;

    private long accountA;
    private long accountB;

    private String symbol;

    private double price;
    private double volume;

    // 交易后的差价（利润）,负值代表亏损
    private double eatDiff;
    private double eatDiffPercent;

    private TradeDirect direct;

    private double totalMoney;

    // 盈利百分比
    private double profit;
}
