package com.jordan.ban.dao;

import com.jordan.ban.entity.Balance;
import com.jordan.ban.entity.Platform;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface BalanceRepository extends CrudRepository<Balance, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Balance balance set balance.available =:available, balance.balance=:balance where balance.currency =:currency")
    void updateBalance(double available, double balance, String currency);

    Balance queryByCurrencyAndPlatform(String currency,Platform platform);
}
