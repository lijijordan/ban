package com.jordan.ban.service;

import com.jordan.ban.ProductApplication;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.TradeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    public static double USD_MONEY = 20000;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

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
        initAccount(ProductApplication.huobi, ProductApplication.gateio, "EOS_USDT", 14.5632);
    }


    public void emptyAccount() {
        this.tradeRecordRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    public void initAccount(String market1, String market2, String symbol, double price) {
        this.create(market1, symbol, price);
        this.create(market2, symbol, price);
    }

    private void create(String platform, String symbol, double price) {
        if (accountRepository.findBySymbolAndPlatform(symbol, platform) != null) {
            System.out.println(String.format("%s, %s has already created!", symbol, platform));
            return;
        }
        double coin = USD_MONEY / price / 2;
        double money = USD_MONEY - (coin * price);
        Account account = Account.builder().platform(platform).symbol(symbol).money(money).virtualCurrency(coin).build();
        this.accountRepository.save(account);
    }


}
