package com.jordan.ban.dao;

import com.jordan.ban.entity.Grid;
import com.jordan.ban.entity.SingleGrid;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SingleGridRepository extends CrudRepository<SingleGrid, Long> {

    @Query(value = "select * from grid where high>=?1 and low<=?1 and symbol=?2 ", nativeQuery = true)
    SingleGrid find(double diffPercent, String symbol);

    List<SingleGrid> findAllBySymbol(String symbol);
}
