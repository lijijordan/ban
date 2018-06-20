package com.jordan.ban.dao;

import com.jordan.ban.entity.Account;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

public interface AccountRepository extends CrudRepository<Account, Long> {

    @Query(value = "select * from account where platform = ?2 and symbol = ?1", nativeQuery = true)
    Account findBySymbolAndPlatform(String symbol, String diffPlatform);
}
