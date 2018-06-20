package service;


import com.jordan.ban.TacticsApplication;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TacticsApplication.class)
@Slf4j
public class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Test
    public void findBalances() {
        Map<String, BalanceDto> map = this.accountService.findBalancesCache(Huobi.PLATFORM_NAME);
        Assert.assertNotNull(map);
    }

    @Test
    public void queryAccount() {
        Map<String, BalanceDto> huobiBalance = accountService.findBalancesCache(Huobi.PLATFORM_NAME);
        AccountDto accountA = AccountDto.builder().money(huobiBalance.get("usdt").getBalance()).platform(Huobi.PLATFORM_NAME).symbol("ltcusdt")
                .virtualCurrency(huobiBalance.get("ltc").getBalance()).build();
        Map<String, BalanceDto> fcoinBalance = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        AccountDto accountB = AccountDto.builder().money(fcoinBalance.get("usdt").getBalance()).platform(Fcoin.PLATFORM_NAME).symbol("ltcusdt")
                .virtualCurrency(fcoinBalance.get("ltc").getBalance()).build();

        System.out.println(accountA);
        System.out.println(accountB);

        System.out.println(accountA.getVirtualCurrency()-accountB.getVirtualCurrency());

        System.out.println("total money:" + (accountA.getMoney() + accountB.getMoney()));
        System.out.println("total coin:" + (accountA.getVirtualCurrency() + accountB.getVirtualCurrency()));
    }
}
