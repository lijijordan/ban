package com.jordan.ban.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account extends BaseEntity {

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
