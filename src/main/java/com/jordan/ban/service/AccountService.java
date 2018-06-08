package com.jordan.ban.service;

import com.jordan.ban.ProductApplication;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.ws.ServiceMode;
import java.security.AccessControlContext;
import java.util.HashMap;

@Service
public class AccountService {

    private double RMB_MONEY = 10000;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * // huobi vs dragonex
     * diffTask(neousdt, huobi, dragonex, 2000);
     * diffTask(eosusdt, huobi, dragonex, 2000);
     * diffTask(btcusdt, huobi, dragonex, 2000);
     * diffTask(eoseth, huobi, dragonex, 2000);
     * <p>
     * // huobi vs okex
     * diffTask(btcusdt, huobi, okex, 2000);
     * diffTask(eosusdt, huobi, okex, 2000);
     * diffTask(neousdt, huobi, okex, 2000);
     * diffTask(eosbtc, huobi, okex, 2000);
     * diffTask(eoseth, huobi, okex, 2000);
     * diffTask(omgeth, huobi, okex, 2000);
     * <p>
     * // huobi vs gateio
     * diffTask(eosbtc, gateio, huobi, 2000);
     * diffTask(eoseth, gateio, huobi, 2000);
     * diffTask(eosusdt, gateio, huobi, 2000);
     * // bit-z vs gateio
     * diffTask(eosbtc, gateio, bitz, 2000);
     * //        diffTask(ltcbtc, gateio, bitz, 2000);
     * <p>
     * // bit-z vs dragonex
     * //        diffTask(gxseth, dragonex, bitz, 2000);
     * <p>
     * // exmo vs drgonex
     * diffTask(bchusdt, dragonex, exmo, 2000);
     * diffTask(eosusdt, dragonex, exmo, 2000);
     */
    public void initAccount() {
        initAccount(ProductApplication.huobi, ProductApplication.gateio, ProductApplication.eosusdt, 89.12);
    }

    private void initAccount(String market1, String market2, String symbol, double price) {
        this.create(market1, symbol, price);
        this.create(market2, symbol, price);
    }

    private void create(String platform, String symbol, double price) {
        double coin = RMB_MONEY / price / 2;
        double money = RMB_MONEY - (coin * price);
        Account account = Account.builder().platform(platform).symbol(symbol).money(money).virtualCurrency(coin).build();
        this.accountRepository.save(account);
    }


}
