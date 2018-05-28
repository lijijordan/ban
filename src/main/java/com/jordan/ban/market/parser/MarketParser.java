package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Symbol;

import java.io.IOException;

/**
 * User: liji
 * Date: 18/5/21
 * Time: 下午2:49
 */
public interface MarketParser {

    void parse();

    Symbol parse(String symbol);

    Symbol parse(String symbol, int symbolId);
}
