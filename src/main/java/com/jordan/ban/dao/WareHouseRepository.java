package com.jordan.ban.dao;

import com.jordan.ban.domain.WareHouseState;
import com.jordan.ban.entity.WareHouse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface WareHouseRepository extends CrudRepository<WareHouse, Long> {


    List<WareHouse> findAllByStateIsNotAndSymbol(WareHouseState wareHouseState, String symbol);
}
