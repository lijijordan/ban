package com.jordan.ban.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridOperationalLog extends BaseEntity {

    public static final String OPERATIONAL_TYPE_PLUS = "PLUS";
    public static final String OPERATIONAL_TYPE_LESS = "LESS";

    private Long gridId;

    private double matchVolume;

    private Long wareHouseId;

    private String operationalType;

}
