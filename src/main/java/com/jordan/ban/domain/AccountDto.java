package com.jordan.ban.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto {

    private Long ID;

    private String platform;

    /**
     * USDT
     */
    private double money;

    /**
     * FIXME convert to object
     */
    private double virtualCurrency;

    private String symbol;

}
