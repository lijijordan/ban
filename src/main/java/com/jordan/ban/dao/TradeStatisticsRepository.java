package com.jordan.ban.dao;

import com.jordan.ban.entity.TradeRecord;
import com.jordan.ban.entity.TradeStatistics;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TradeStatisticsRepository extends CrudRepository<TradeStatistics, Long> {

    // FIXME: add policy id for group by market
    TradeStatistics findTopByOrderByCreateTimeDesc();

}
