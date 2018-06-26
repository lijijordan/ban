package com.jordan.ban.domain;

import com.jordan.ban.entity.Balance;
import com.jordan.ban.entity.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDto {

    private String currency;
    // 可以使用的
    private double available;
    private double frozen;
    private double balance;

    public Balance toBalance(Platform platform) {
        return Balance.builder().platform(platform).available(available)
                .balance(balance).frozen(frozen).currency(currency).build();
    }

}
