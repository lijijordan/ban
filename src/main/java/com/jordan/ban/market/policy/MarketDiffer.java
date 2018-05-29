package com.jordan.ban.market.policy;

import com.jordan.ban.domain.Differ;
import com.jordan.ban.domain.Symbol;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import com.jordan.ban.mq.MessageSender;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.java.Log;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Log
public class MarketDiffer {

    private CompletionService<Symbol> completionService;
    private ExecutorService executorService;


    public MarketDiffer() {
        executorService = Executors.newCachedThreadPool();
        completionService = new ExecutorCompletionService(executorService);
    }

    public Differ differ(String symbol, String marketName1, String marketName2) throws InterruptedException, ExecutionException, IOException {
        long start = System.currentTimeMillis();
        completionService.submit(getSymbol(marketName1, symbol));
        completionService.submit(getSymbol(marketName2, symbol));
        Symbol symbol1 = completionService.take().get();
        Symbol symbol2 = completionService.take().get();
        System.out.println(symbol1);
        System.out.println(symbol2);
        System.out.println("CompletionService all done.");
        Differ differ = this.diffSymbol(symbol1, symbol2);
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
            DecimalFormat df = new DecimalFormat("##.##%");
            String formattedPercent = df.format(differ);
            differObject = new Differ();
            differObject.setSymbol(symbol1.getSymbol());
            differObject.setDiffer((float) differ);
            differObject.setPercentDiffer(formattedPercent);
            differObject.setCreateTime(new Date());
            differObject.setDifferPlatform(symbol1.getPlatform() + "-" + symbol2.getPlatform());
        }
        return differObject;
    }


    public void shutdown() {
        this.executorService.shutdown();
    }

    public Callable<Symbol> getSymbol(String market, String symbol) {
        MarketParser marketParser = MarketFactory.getMarket(market);
        Callable<Symbol> task = () -> marketParser.getPrice(symbol);
        return task;
    }
}
