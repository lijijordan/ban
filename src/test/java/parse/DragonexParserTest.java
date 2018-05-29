package parse;

import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.Dragonex;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Log
public class DragonexParserTest {

    private Dragonex dragonexParser;

    @Before
    public void init() {
        dragonexParser = new Dragonex();
    }

    @Test
    public void testParse() {
        Assert.assertTrue(true);
        Symbol symbol = dragonexParser.getPrice("NEOUSDT");
        Assert.assertNotNull(symbol);
        log.info(symbol.toString());
    }
}
