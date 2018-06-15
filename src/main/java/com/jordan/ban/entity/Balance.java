package com.jordan.ban.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * {
 * "currency": "btc",
 * "available": "50.0",
 * "frozen": "50.0",
 * "balance": "100.0"
 * }
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Balance extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private Platform platform;

    private String currency;
    private double available;
    private double frozen;
    private double balance;
}
