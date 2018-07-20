package com.jordan.ban.dao;

import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GridRepository extends CrudRepository<Grid, Long> {

    @Query(value = "select * from grid where high>=?1 and low<=?1 and symbol=?2 ", nativeQuery = true)
    Grid find(double diffPercent, String symbol);


}
