package com.jordan.ban.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MockTradeResultIndex {

    /**
     * 价差
     */
    private double eatDiff;
    /**
     * 最小收益率：(A-B/A+B)
     */
    private double eatPercent;

    private double tradeDiff;

    private double tradePercent;

    private TradeDirect tradeDirect;

    private Date createTime;

    private long costTime;

    private String symbol;

    private String diffPlatform;

    //交易数量
    private double eatTradeVolume;

    // 交易费用

    private double sellCost;
    private double buyCost;
    private double tradeVolume;

    private double sellPrice;
    private double buyPrice;

    @JsonIgnore
    public String getPlatformA() {
        if (StringUtils.isEmpty(this.diffPlatform)) {
            return null;
        }
        return diffPlatform.split("-")[0];
    }

    @JsonIgnore
    public String getPlatformB() {
        if (StringUtils.isEmpty(this.diffPlatform)) {
            return null;
        }
        return diffPlatform.split("-")[1];
    }

    @JsonIgnore
    public String getMoney() {

        return this.symbol.replace("_", "").substring(3, symbol.replace("_", "").length());
    }

    @JsonIgnore
    public String getCurrency() {
        return this.symbol.replace("_", "").substring(0, 3);
    }


}
