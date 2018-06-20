package com.jordan.ban.service;

import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.BalanceRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.entity.Account;
import com.jordan.ban.market.parser.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private Map<String, Map<String, BalanceDto>> balanceCache = new ConcurrentHashMap<>();
    public static double USD_MONEY = 20000;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private BalanceRepository balanceRepository;

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
    public void mockAccountTestData() {
        initAccount(Huobi.PLATFORM_NAME, Gateio.PLATFORM_NAME, "EOS_USDT", 14.5632);


        initAccount(Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, "LTCUSDT", 112.610000000);
        initAccount(Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, "BCHUSDT", 1032.690000000);
        initAccount(Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME, "ETHUSDT", 572.300000000);

        initAccount(Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, "EOSUSDT", 13.1469);
        initAccount(Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, "NEOUSDT", 48.4760);
        initAccount(Huobi.PLATFORM_NAME, Dragonex.PLATFORM_NAME, "ETHUSDT", 572.1195);
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

    /**
     * Find all currency balances.
     *
     * @param platformName
     * @return
     */
    // TODO:use cache
    public Map<String, BalanceDto> updateBalancesCache(String platformName) {
        Map<String, BalanceDto> map = new HashMap<>();
        MarketParser market = MarketFactory.getMarket(platformName);
        if (market instanceof Huobi) {
            Huobi huobi = (Huobi) market;
            long accountId = huobi.getAccountID();
            huobi.getBalances(accountId).forEach(balanceDto -> {
                map.put(balanceDto.getCurrency(), balanceDto);
            });
        } else if (market instanceof Fcoin) {
            ((Fcoin) market).getBalances().forEach(balanceDto -> {
                map.put(balanceDto.getCurrency(), balanceDto);
            });
        }
        if (!map.isEmpty()) {
            balanceCache.put(platformName, map);
        }
        return map;
    }

    public Map<String, BalanceDto> findBalancesCache(String platformName) {
        if (this.balanceCache.get(platformName) == null) {
            return this.updateBalancesCache(platformName);
        } else {
            return this.balanceCache.get(platformName);
        }
    }


}
