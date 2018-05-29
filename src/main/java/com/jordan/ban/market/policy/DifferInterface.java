package com.jordan.ban.market.policy;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.market.parser.MarketParser;

public interface DifferInterface {

    Differ differ(MarketParser marketParser1, MarketParser marketParser2);
}
