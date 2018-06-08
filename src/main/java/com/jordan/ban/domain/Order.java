package com.jordan.ban.domain;

import lombok.Data;

@Data
public class Order {

    private long id;
    private String platform;
    private AccountDto account;

    private double price;
    private long timestamp;

    private double volume;

    private double tradeVolume;
}
