package parse;

import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.DragonexParser;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@Log
public class DragonexParserTest {

    private DragonexParser dragonexParser;

    @Before
    public void init() {
        dragonexParser = new DragonexParser();
    }

    @Test
    public void testParse() {
        Assert.assertTrue(true);
        Symbol symbol = dragonexParser.parse("NEOUSDT",129);
        Assert.assertNotNull(symbol);
        log.info(symbol.toString());
    }
}
