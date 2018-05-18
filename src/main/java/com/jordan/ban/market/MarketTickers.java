package com.jordan.ban.market;

import com.jordan.ban.domain.Symbol;

import java.util.Map;

/**
 * User: liji
 * Date: 18/5/18
 * Time: 上午10:10
 */
public interface MarketTickers {

    Map<String, Symbol> getTickers();
}
