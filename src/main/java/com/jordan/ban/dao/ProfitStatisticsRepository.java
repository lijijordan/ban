package com.jordan.ban.dao;

import com.jordan.ban.entity.ProfitStatistics;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProfitStatisticsRepository extends CrudRepository<ProfitStatistics, Long> {

    ProfitStatistics findTopBySymbolAndAndPlatformAAndAndPlatformBOrderByCreateTimeDesc(String symbol, String platformA, String platformB);

    List<ProfitStatistics> findAllByOrderByCreateTimeDesc();

}
