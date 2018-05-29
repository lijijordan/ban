package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Stacks;
import com.jordan.ban.domain.Symbol;

import java.io.IOException;

/**
 * User: liji
 * Date: 18/5/21
 * Time: 下午2:49
 */
public interface MarketParser {

    Symbol getPrice(String symbol);

    Symbol getPrice(String symbol, int symbolId);

    Stacks getStacks(String symbol);

}
