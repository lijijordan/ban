package service;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.entity.SingleGrid;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.SingleGridService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class SingleGridServiceTest {

    @Autowired
    private SingleGridService singleGridService;

    @Test
    public void test_GenerateSingleGrid() {
        singleGridService.generateSingleGrid(100, 163.88, "ethusdt", 0.1f, 7.1f, Fcoin.PLATFORM_NAME);
    }



}
