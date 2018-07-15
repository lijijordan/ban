package com.jordan.ban.service;

import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.dao.TradeStatisticsRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.entity.TradeRecord;
import com.jordan.ban.entity.TradeStatistics;
import com.jordan.ban.exception.StatisticException;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.jordan.ban.common.Constant.USDT;

@Slf4j
@Service
public class TradeRecordService {

    @Autowired
    TradeRecordRepository tradeRecordRepository;

    public StatisticRecordDto queryAndStatisticTradeRecord(Date startTime) {
        List<TradeRecord> list = this.tradeRecordRepository.findAllByCreateTime(startTime);
        double sumCostMoney = 0;
        double sumProfit = 0;
        double avgA2BDiffPercent = 0;
        double avgB2ADiffPercent = 0;
        double sumA2BDiffPercent = 0;
        double sumB2ADiffPercent = 0;
        double countA2B = 0;
        double countB2A = 0;
        double sumA2BProfit = 0;
        double sumB2AProfit = 0;
        double sumA2BVolume = 0;
        double sumB2AVolume = 0;

        double avgA2BProfit = 0;
        double avgB2AProfit = 0;
        for (int i = 0; i < list.size(); i++) {
            TradeRecord tradeRecord = list.get(i);
            sumCostMoney += tradeRecord.getTradeCostMoney();
            sumProfit += tradeRecord.getProfit();
            if (tradeRecord.getDirect() == TradeDirect.A2B) {
                countA2B++;
                sumA2BProfit += tradeRecord.getProfit();
                sumA2BDiffPercent += tradeRecord.getEatDiffPercent();
                sumA2BVolume += tradeRecord.getVolume();
            } else {
                countB2A++;
                sumB2AProfit += tradeRecord.getProfit();
                sumB2ADiffPercent += tradeRecord.getEatDiffPercent();
                sumB2AVolume += tradeRecord.getVolume();
            }
        }
        avgB2ADiffPercent = sumB2ADiffPercent / countB2A;
        avgA2BDiffPercent = sumA2BDiffPercent / countA2B;

        avgA2BProfit = sumA2BVolume / countA2B;
        avgB2AProfit = sumB2AVolume / countB2A;
        return StatisticRecordDto.builder().recordList(list).sumA2BProfit(sumA2BProfit).sumB2AProfit(sumB2AProfit)
                .sumCostMoney(sumCostMoney).sumProfit(sumProfit).avgA2BProfit(avgA2BProfit).avgB2AProfit(avgB2AProfit)
                .avgA2BDiffPercent(avgA2BDiffPercent).avgB2ADiffPercent(avgB2ADiffPercent).build();

    }


}
