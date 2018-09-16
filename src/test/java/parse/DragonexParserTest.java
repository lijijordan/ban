package parse;

import com.jordan.ban.domain.Symbol;
import com.jordan.ban.market.parser.Dragonex;
import lombok.extern.java.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

@Log
public class DragonexParserTest {

    private Dragonex dragonexParser;

    @Before
    public void init() {
        try {
            dragonexParser = new Dragonex();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParse() {
        Assert.assertTrue(true);
        Symbol symbol = dragonexParser.getPrice("NEOUSDT");
        Assert.assertNotNull(symbol);
        log.info(symbol.toString());
    }
}
