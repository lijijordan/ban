package com.jordan.ban.market.parser;

import com.carrotsearch.hppc.ByteArrayList;
import com.jordan.ban.domain.*;

import java.io.IOException;
import java.util.List;

/**
 * User: liji
 * Date: 18/5/21
 * Time: 下午2:49
 */
public interface MarketParser {

    String getName();

    Symbol getPrice(String symbol);

    Depth getDepth(String symbol);

    BalanceDto getBalance(String symbol);

    String placeOrder(OrderRequest request);

    OrderResponse getFilledOrder(String orderId);

    boolean cancelOrder(String orderId);

}
