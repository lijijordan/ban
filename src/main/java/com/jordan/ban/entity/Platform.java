package com.jordan.ban.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.List;

/**
 * 交易市场/平台
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Platform extends BaseEntity {

    private String name;

    @OneToMany(mappedBy = "platform", fetch = FetchType.LAZY)
    private List<Balance> balances;

    @OneToMany(mappedBy = "platform", fetch = FetchType.LAZY)
    private List<Order> orders;
}
