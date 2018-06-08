package com.jordan.ban.domain;

import lombok.Data;

@Data
public class AccountDto {

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
