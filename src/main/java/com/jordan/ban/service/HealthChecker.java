package com.jordan.ban.service;

import com.jordan.ban.market.TradeCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HealthChecker {

    @Autowired
    private TradeCounter tradeCounter;

    private double lastDiffPercent;

    private int errorCount;

    @Autowired
    private SlackService slackService;

    private static final int MAX_ERROR_COUNT = 50;

    public void check() {
        double d = tradeCounter.getCurrentDiffPercent();
        if (d == lastDiffPercent) {
            errorCount++;
        }
        lastDiffPercent = d;
        if (errorCount > MAX_ERROR_COUNT) {
            this.slackService.sendMessage("Error", "Please check your service. [" + errorCount + "]");
            errorCount = 0;
        }
    }

}
