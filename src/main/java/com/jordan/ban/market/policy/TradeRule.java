package com.jordan.ban.market.policy;

public interface TradeRule {

    /**
     * weather trade it
     * @return
     */
    boolean canDeal();
}
