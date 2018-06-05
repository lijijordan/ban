package com.jordan.ban.market.parser;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.domain.Symbol;

import java.io.IOException;

/**
 * User: liji
 * Date: 18/5/21
 * Time: 下午2:49
 */
public interface MarketParser {

    String getName();

    Symbol getPrice(String symbol);

    Depth getDepth(String symbol);
}
