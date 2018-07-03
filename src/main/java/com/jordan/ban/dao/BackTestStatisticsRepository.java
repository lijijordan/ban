package com.jordan.ban.dao;

import com.jordan.ban.entity.BackTestStatistics;
import com.jordan.ban.entity.Balance;
import com.jordan.ban.entity.Platform;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface BackTestStatisticsRepository extends CrudRepository<BackTestStatistics, Long> {

}
