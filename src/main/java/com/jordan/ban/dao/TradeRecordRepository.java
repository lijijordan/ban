package com.jordan.ban.dao;

import com.jordan.ban.entity.TradeRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface TradeRecordRepository extends CrudRepository<TradeRecord, Long> {

    @Query(value = "SELECT AVG(eat_diff_percent) FROM trade_record WHERE eat_diff_percent>0 AND accountA=?1 AND accountB=?2 AND symbol=?3",
            nativeQuery = true)
    Double avgEatDiffPercent(long accountA, long accountB, String symbol);


    @Query(value = "select * from trade_record where create_time>?1 order by create_time desc;", nativeQuery = true)
    List<TradeRecord> findAllByCreateTime(Date createTime);

    int countBy();
}
