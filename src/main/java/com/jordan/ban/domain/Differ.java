package com.jordan.ban.domain;

import lombok.Data;

import java.util.Date;

/**
 * User: liji
 * Date: 18/5/16
 * Time: 下午3:24
 */
@Data
public class Differ {

    private String symbol;
    /**
     * compare result float
     */
    private float differ;
    /**
     * compare result by percent format.
     */
    private String percentDiffer;
    /**
     * create createTime
     */
    private Date createTime;
    /**
     * compare platform name
     */
    private String differPlatform;

    /**
     * Differ cost time as long
     */
    private long diffCostTime;
}
