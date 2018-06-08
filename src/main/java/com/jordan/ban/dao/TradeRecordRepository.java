package com.jordan.ban.dao;

import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TradeRecordRepository extends CrudRepository<TradeRecord, Long> {

    @Query(value = "SELECT AVG(EAT_DIFF_PERCENT) FROM TRADE_RECORD WHERE ACCOUNTA=?1 AND ACCOUNTB=?2 AND SYMBOL=?3",
            nativeQuery = true)
    double avgEatDiffPercent(long accountA, long accountB, String symbol);
}
