package parse;

import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.Huobi;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Log
public class HuobiParserTest {

    private Huobi huobiParser;

    @Before
    public void init() {
        huobiParser = new Huobi("","");
    }

    @Test
    public void testParse() {
        Assert.assertTrue(true);
        Symbol symbol = huobiParser.getPrice("neousdt");
        Assert.assertNotNull(symbol);
        log.info(symbol.toString());
    }
}
