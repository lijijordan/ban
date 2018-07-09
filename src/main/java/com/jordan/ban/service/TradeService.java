package com.jordan.ban.service;

import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.TradeRecord;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.FeeUtils;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.TradeCounter;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;

@Service
@Slf4j
public class TradeService {


    //min limit order amount 0.01
    public static final double MIN_TRADE_AMOUNT = 0.01;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TradeContext tradeContext;

    @Autowired
    private TradeCounter tradeCounter;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    public synchronized void preTrade(MockTradeResultIndex tradeResult) {
        // 过滤交易数小于最小交易量的数据
        
        if (!this.tradeCounter.isFull()) { // 池子没有建满，什么都不做
            log.info("Pool is not ready [{}], do nothing!", this.tradeCounter.getSize());
            this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
        } else {
            boolean isTrade = this.trade(tradeResult);
            if (!isTrade) { // 交易的数据不记录到Counter
                this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
            }
        }
    }

    public boolean trade(MockTradeResultIndex tradeResult) {

        MarketParser marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        MarketParser marketB = MarketFactory.getMarket(tradeResult.getPlatformB());

        String symbol = tradeResult.getSymbol().toLowerCase();
        String coinName = symbol.replace("usdt", "");

        AccountDto accountA, accountB;
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(marketA.getName());
        Map<String, BalanceDto> balanceB = accountService.findBalancesCache(marketB.getName());

        accountA = AccountDto.builder().money(balanceA.get("usdt").getAvailable()).platform(marketA.getName()).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getAvailable() : 0).build();
        accountB = AccountDto.builder().money(balanceB.get("usdt").getAvailable()).platform(marketB.getName()).symbol(symbol)
                .virtualCurrency(balanceB.get(coinName) != null ? balanceB.get(coinName).getAvailable() : 0).build();

        if (accountA == null || accountB == null) {
            throw new TradeException("Load account error！");
        }
        double moneyBefore = accountA.getMoney() + accountB.getMoney();
        // 最小交易量
        double minTradeVolume = tradeResult.getEatTradeVolume();
        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        double canBuyCoin;
        //计算最小量
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // B市场卖出币量
            if (accountB.getVirtualCurrency() < minTradeVolume) {
                minTradeVolume = accountB.getVirtualCurrency();
            }
            // 市场A可买入币量
            canBuyCoin = accountA.getMoney() / (buyPrice * (1 + FeeUtils.getFee(marketA.getName())));


        } else {  // 市场B买. 市场A卖
            if (accountA.getVirtualCurrency() < minTradeVolume) {
                minTradeVolume = accountA.getVirtualCurrency();
            }
            // 市场B可买入币量
            canBuyCoin = accountB.getMoney() / (buyPrice * (1 + FeeUtils.getFee(marketB.getName())));
        }
        if (canBuyCoin < minTradeVolume) {
            minTradeVolume = canBuyCoin;
        }
        if (minTradeVolume <= 0) {
            log.info("trade volume is 0!");
            return false;
        }
        if (minTradeVolume <= MIN_TRADE_AMOUNT) {
//            log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
            return false;
        }


        double sellCost = (sellPrice * minTradeVolume) - (sellPrice * minTradeVolume * TRADE_FEES);
        double buyCost = (buyPrice * minTradeVolume) + (buyPrice * minTradeVolume * TRADE_FEES);

        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // TODO:计算交易数量
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() + minTradeVolume);
            accountA.setMoney(accountA.getMoney() - buyCost);
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() - minTradeVolume);
            accountB.setMoney(accountB.getMoney() + sellCost);
        } else {  // 市场B买. 市场A卖
            accountB.setVirtualCurrency(accountB.getVirtualCurrency() + minTradeVolume);
            accountB.setMoney(accountB.getMoney() - buyCost);
            accountA.setVirtualCurrency(accountA.getVirtualCurrency() - minTradeVolume);
            accountA.setMoney(accountA.getMoney() + sellCost);
        }

        if (accountA.getMoney() < 0 || accountB.getMoney() < 0) {
//            log.info("Money is not enough！!");
            return false;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
//            log.info("Coin is not enough！!");
            return false;
        }

//        log.info("============================ VALIDATE ============================");
        double upMax = tradeCounter.getMaxDiffPercent(true);
        double downMax = tradeCounter.getMaxDiffPercent(false);
        if (upMax == 0 || downMax == 0) {
            log.info("Counter queue is not ready!");
            return false;
        }

        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();
        // FIXME:Do not use direct A2B;
        // Validate
        if (TradeDirect.A2B == tradeResult.getTradeDirect()) {
            if (diffPercent < tradeContext.getDownPoint()) {
                return false;
            } else {
                if (diffPercent < downMax) {
                    return false;
                }
            }
        } else {
            if (diffPercent < tradeContext.getUpPoint()) {
                return false;
            } else {
                if (diffPercent < upMax) {
                    return false;
                }
            }
        }
        log.info("============================ PLACE ORDER ============================");
        long start = System.currentTimeMillis();
        double profit = moneyAfter - moneyBefore;
        // 统一精度4
        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            double fees = 1 - FeeUtils.getFee(marketA.getName());
            buyOrder.setAmount(buyOrder.getAmount() / fees);
            orderService.createOrder(buyOrder, marketA, pair, tradeResult.getTradeDirect(), diffPercent);
            orderService.createOrder(sellOrder, marketB, pair, tradeResult.getTradeDirect(), diffPercent);
        } else {  // 市场B买. 市场A卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            double fees = 1 - FeeUtils.getFee(marketB.getName());
            buyOrder.setAmount(buyOrder.getAmount() / fees);
            orderService.createOrder(sellOrder, marketA, pair, tradeResult.getTradeDirect(), diffPercent);
            orderService.createOrder(buyOrder, marketB, pair, tradeResult.getTradeDirect(), diffPercent);
        }
        log.info("Trade done!");
        // 记录Record
        log.info("Record trade information:");
        double totalMoney = accountA.getMoney() + accountB.getMoney();
        TradeRecord record = new TradeRecord();
        record.setPlatformA(accountA.getPlatform());
        record.setPlatformB(accountB.getPlatform());
        record.setDirect(tradeResult.getTradeDirect());
        record.setEatDiff(tradeResult.getEatDiff());
        record.setEatDiffPercent(tradeResult.getEatPercent());
        record.setSymbol(tradeResult.getSymbol());
        record.setTradeTime(tradeResult.getCreateTime());
        record.setVolume(tradeResult.getEatTradeVolume());
        record.setProfit(profit);
        record.setTradeCostMoney(buyCost + sellCost);
        record.setTotalMoney(totalMoney);
        this.tradeRecordRepository.save(record);
        log.info("Record done! cost time:[{}]s", (System.currentTimeMillis() - start) * 1000);
        return true;
    }
}
