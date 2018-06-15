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
public class MockResult {
    private double profit;
    private TradeDirect direct;
    private String platformA;
    private String platformB;

    
}
