import com.jordan.ban.BanApplication;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.service.TradeService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BanApplication.class)
public class TradeServiceTest {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testTrade1() {
        Assert.assertNotNull(tradeService);

    }

    @Test
    public void searchAccount(){
        Assert.assertNotNull(this.accountRepository.findById(1l));
        Assert.assertNotNull(this.accountRepository.findBySymbolAndPlatform("EOSUSDT","Huobi"));
    }
}
