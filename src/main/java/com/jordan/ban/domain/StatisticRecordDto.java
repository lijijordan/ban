package com.jordan.ban.domain;


import com.jordan.ban.entity.TradeRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatisticRecordDto {

    private List<TradeRecord> recordList;

    private double sumCostMoney;

    private double sumProfit;

    private double avgA2BDiffPercent;

    private double avgB2ADiffPercent;

    private double sumA2BProfit;

    private double sumB2AProfit;

    private double avgA2BProfit;
    private double avgB2AProfit;
}
