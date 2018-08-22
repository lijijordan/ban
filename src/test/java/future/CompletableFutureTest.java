package future;

import com.jordan.ban.domain.Depth;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import org.apache.http.util.Asserts;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.jordan.ban.common.Constant.ETH_USDT;

public class CompletableFutureTest {


    @Test
    public void test1() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        try {
            System.out.println(future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("CompletableFuture");
    }


    @Test
    public void testParseAsync() {
        MarketParser marketA = MarketFactory.getMarket(Dragonex.PLATFORM_NAME);
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            Depth d = marketA.getDepth(ETH_USDT);
            System.out.println("cost time:" + (System.currentTimeMillis() - start) + " ms");
            Assert.assertNotNull(d);
            System.out.println(d);
        }
    }

}
