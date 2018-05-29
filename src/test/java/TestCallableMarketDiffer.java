import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;

import java.util.Random;
import java.util.concurrent.*;


public class TestCallableMarketDiffer {

    public static void main(String[] args) {
        String symbol = "NEOUSDT";
        try {
            completionServiceCount(symbol);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用completionService收集callable结果
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void completionServiceCount(String symbol) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletionService<Symbol> completionService = new ExecutorCompletionService(executorService);

        completionService.submit(getSymbol("Dragonex", symbol));
        completionService.submit(getSymbol("Huobi", symbol));

        Symbol symbol1 = completionService.take().get();
        Symbol symbol2 = completionService.take().get();

        System.out.println(symbol1);
        System.out.println(symbol2);
        System.out.println("CompletionService all done.");
        executorService.shutdown();
    }

    public static Callable<Symbol> getSymbol(String market, String symbol) {
        MarketParser marketParser = MarketFactory.getMarket(market);
        Callable<Symbol> task = () -> marketParser.getPrice(symbol, 129);
        return task;
    }
}