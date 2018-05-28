package parse;

import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.DragonexParser;
import com.jordan.ban.market.parser.HuobiParser;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Log
public class HuobiParserTest {

    private HuobiParser huobiParser;

    @Before
    public void init() {
        huobiParser = new HuobiParser();
    }

    @Test
    public void testParse() {
        Assert.assertTrue(true);
        Symbol symbol = huobiParser.parse("neousdt");
        Assert.assertNotNull(symbol);
        log.info(symbol.toString());
    }
}
