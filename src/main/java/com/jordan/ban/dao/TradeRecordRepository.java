package com.jordan.ban.dao;

import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.data.repository.CrudRepository;

public interface TradeRecordRepository extends CrudRepository<TradeRecord, Long> {
}
