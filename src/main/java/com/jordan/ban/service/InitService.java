package com.jordan.ban.service;

import com.jordan.ban.dao.BalanceRepository;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.entity.Balance;
import com.jordan.ban.entity.Platform;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InitService {

    @Autowired
    private PlatformService platformService;


    @Autowired
    private BalanceRepository balanceRepository;

    private void updateBalance(BalanceDto balanceDto, Platform platform) {
        Balance balance = this.balanceRepository.queryByCurrencyAndPlatform(balanceDto.getCurrency(), platform);
        if (balance == null) {
            this.balanceRepository.save(balanceDto.toBalance(platform));
        } else {
//            BeanUtils.copyProperties(balanceDto, balance);
            log.info(balanceDto.toString());
            balance.setBalance(balanceDto.getBalance());
            this.balanceRepository.save(balance);
        }
    }

    public void init(String platformName) {
        Platform platform = this.platformService.createPlatform(platformName);
        MarketParser market = MarketFactory.getMarket(platformName);
        if (market instanceof Huobi) {
            Huobi huobi = (Huobi) market;
            long accountId = huobi.getAccountID();
            huobi.getBalances(accountId).forEach(balanceDto -> {
                this.updateBalance(balanceDto, platform);
            });
        } else if (market instanceof Fcoin) {
            ((Fcoin) market).getBalances().forEach(balanceDto -> {
                this.updateBalance(balanceDto, platform);
            });
        }
    }


}
