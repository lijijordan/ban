package com.jordan.ban.entity;

import com.jordan.ban.domain.WareHouseState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WareHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;

    private double diffPercentIn;

    private double diffPercentOut;

    private double volumeIn;

    private double volumeOut;

    private WareHouseState state;

    private Date timeIn;

    private Date timeOut;

    private Long gridId;

    private String symbol;

}
