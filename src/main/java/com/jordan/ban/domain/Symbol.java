package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

/**
 * User: liji
 * Date: 18/5/16
 * Time: 下午2:11
 */
@Data
public class Symbol {

    private String symbol;

    private Date time;

    private double price;

    private String platform;
}
