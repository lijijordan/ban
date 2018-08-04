package com.jordan.ban.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TradeCounter {


    private static double currentDiffPercent;

    public static double getCurrentDiffPercent() {
        return currentDiffPercent;
    }
    public static void setCurrentDiffPercent(double currentDiffPercent) {
        TradeCounter.currentDiffPercent = currentDiffPercent;
    }

}
