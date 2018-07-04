package service;


import com.jordan.ban.BackTestApplication;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.InitService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BackTestApplication.class)
@Slf4j
public class InitServiceTest {


    @Autowired
    private InitService initService;

    @Test
    public void initFcoin() {
        initService.init(Fcoin.PLATFORM_NAME);
    }

    @Test
    public void initHuobi() {
        initService.init(Huobi.PLATFORM_NAME);
    }
}
