package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Depth {

    private String symbol;

    private String platform;

    private Date time;

    private List<Ticker> bids;

    private List<Ticker> asks;

}
