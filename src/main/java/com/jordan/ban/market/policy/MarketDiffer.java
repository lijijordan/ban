package com.jordan.ban.market.policy;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.*;

@Log
public class MarketDiffer {


    public Differ differ(String symbol, String marketName1, String marketName2) throws InterruptedException, ExecutionException, IOException {
        long start = System.currentTimeMillis();
//        completionService.submit(getSymbol(marketName1, symbol));
//        completionService.submit(getSymbol(marketName2, symbol));
//        Symbol symbol1 = completionService.take().get();
//        Symbol symbol2 = completionService.take().get();
        MarketParser market1 = MarketFactory.getMarket(marketName1);
        MarketParser market2 = MarketFactory.getMarket(marketName2);
//        System.out.println(market1.getPrice(symbol));
//        System.out.println(market2.getPrice(symbol));
//        System.out.println("CompletionService all done.");
        Differ differ = this.diffSymbol(market1.getPrice(symbol), market2.getPrice(symbol));
        if (differ != null) {
            differ.setDiffCostTime(System.currentTimeMillis() - start);
        }
        return differ;
    }


    private Differ diffSymbol(Symbol symbol1, Symbol symbol2) {
        double price1 = symbol1.getPrice();
        Differ differObject = null;
        if (symbol2 != null) {
            double price2 = symbol2.getPrice();
            double differ = (price1 - price2) / Math.max(price1, price2);
            differObject = new Differ();
            differObject.setSymbol(symbol1.getSymbol());
            differObject.setDiffer((float) differ);
            differObject.setCreateTime(new Date());
            differObject.setDifferPlatform(symbol1.getPlatform() + "-" + symbol2.getPlatform());
        }
        return differObject;
    }

    public Callable<Symbol> getSymbol(String market, String symbol) {
        MarketParser marketParser = MarketFactory.getMarket(market);
        Callable<Symbol> task = () -> marketParser.getPrice(symbol);
        return task;
    }
}
