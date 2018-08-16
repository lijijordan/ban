package com.jordan.ban.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Grid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;

    /**
     * 网格区间
     */
    private double low;
    private double high;

    private String symbol;

    // 配额累计币量
    private double volume;

    // 当前剩余的币量
    private double lastVolume;

    //区间的配额
    private float quota;

    private double totalMatch;
}
