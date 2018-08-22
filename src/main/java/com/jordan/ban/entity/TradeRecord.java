package com.jordan.ban.entity;

import com.jordan.ban.domain.TradeDirect;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

@Data
@Entity
public class TradeRecord extends BaseEntity {

    private Date tradeTime;

    private long accountA;
    private String platformA;
    private long accountB;
    private String platformB;

    private String symbol;

    private double price;
    private double volume;

    // 交易后的差价（利润）,负值代表亏损
    private double eatDiff;
    private double eatDiffPercent;

    private TradeDirect direct;

    private double totalMoney;

    // 本次盈利百分比
    private double profit;

    private double totalProfit;

    // 一次交易:costBuy + costSell
    private double tradeCostMoney;


    private float upPercent;
    private float downPercent;

    private double upMax;
    private double downMax;

    private String orderPairKey;

}
