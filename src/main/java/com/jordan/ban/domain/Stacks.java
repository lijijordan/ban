package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Stacks {

    private String platform;

    private Date currentTime;

    private String symbol;

    private BuyStack buyStack;

    private SellStack sellStack;
}
