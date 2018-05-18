package com.jordan.ban.market;

import com.jordan.ban.domain.Symbol;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * User: liji
 * Date: 18/5/18
 * Time: 上午10:14
 */
@Service
public class BittrexTickers implements MarketTickers {

    @Override
    public Map<String, Symbol> getTickers() {
        return null;
    }
}
