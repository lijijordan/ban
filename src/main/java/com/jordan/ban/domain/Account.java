package com.jordan.ban.domain;

import lombok.Data;

@Data
public class Account {

    private long id;

    private String platform;

    /**
     * USDT
     */
    private double money;

    /**
     * FIXME convert to object
     */
    private double virtualCurrency;

    private String name;

    
}
