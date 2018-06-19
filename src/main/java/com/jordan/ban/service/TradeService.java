package com.jordan.ban.service;

import com.jordan.ban.common.Context;
import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.PlatformRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Account;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;

import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;

@Service
@Slf4j
public class TradeService {

    private static double DEFAULT_METRICS_MAX = 0.003; // 0.8%
    private static double METRICS_BACK_PERCENT = 0.7;

    private static double DEFAULT_MIN_TRADE_VOLUME = 1;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    public synchronized void trade(MockTradeResultIndex tradeResult) {
        log.info("Data:" + tradeResult.toString());
        MarketParser marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        MarketParser marketB = MarketFactory.getMarket(tradeResult.getPlatformB());

        Map<String, BalanceDto> huobiBalance = accountService.findBalancesCache(Huobi.PLATFORM_NAME);
        AccountDto accountA = AccountDto.builder().money(huobiBalance.get("usdt").getBalance()).platform(Huobi.PLATFORM_NAME).symbol(tradeResult.getSymbol())
                .virtualCurrency(huobiBalance.get("ltc").getBalance()).build();
        Map<String, BalanceDto> fcoinBalance = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        AccountDto accountB = AccountDto.builder().money(fcoinBalance.get("usdt").getBalance()).platform(Fcoin.PLATFORM_NAME).symbol(tradeResult.getSymbol())
                .virtualCurrency(fcoinBalance.get("ltc").getBalance()).build();

        log.info("账户A: market={}, money={},coin={}", accountA.getPlatform(), accountA.getMoney(), accountA.getVirtualCurrency());
        log.info("账户B: market={}, money={},coin={}", accountB.getPlatform(), accountB.getMoney(), accountB.getVirtualCurrency());
        //FIXME: Mock and test
//        log.info("Mock account!");
//        accountB = AccountDto.builder().money(96.88 * 10).platform(Fcoin.PLATFORM_NAME).symbol(tradeResult.getSymbol()).virtualCurrency(10).build();
        /*
        AccountDto accountA = AccountDto.builder().money(96.88 * 10).platform(Huobi.PLATFORM_NAME).symbol(tradeResult.getSymbol()).virtualCurrency(10).build();
        */

        if (accountA == null || accountB == null) {
            throw new TradeException("加载账户异常！");
        }
        double coinDiffBefore = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyBefore = accountA.getMoney() + accountB.getMoney();

        // 最小交易量
        double minTradeVolume = tradeResult.getEatTradeVolume();
        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        String symbol = tradeResult.getSymbol().toLowerCase();
        double canBuyCoin;
        //计算最小量
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // B市场卖出币量
            if (accountB.getVirtualCurrency() < minTradeVolume) {
                minTradeVolume = accountB.getVirtualCurrency();
            }
            // 市场A可买入币量
            canBuyCoin = accountA.getMoney() / (buyPrice * (1 + 0.002));


        } else {  // 市场B买. 市场A卖
            if (accountA.getVirtualCurrency() < minTradeVolume) {
                minTradeVolume = accountA.getVirtualCurrency();
            }
            // 市场B可买入币量
            canBuyCoin = accountB.getMoney() / (buyPrice * (1 + 0.002));
        }
        if (canBuyCoin < minTradeVolume) {
            minTradeVolume = canBuyCoin;
        }
        if (minTradeVolume <= 0) {
            throw new TradeException("交易量为0!");
        }


        log.info("吃单量：{}", minTradeVolume);
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
            log.info("钱不足！!");
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("币不足！!");
            return;
        }
        Double avgEatDiffPercent = DEFAULT_METRICS_MAX;
        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();
        if (diffPercent < 0) {  // 亏损
            if (coinDiffAfter < coinDiffBefore) {
                if (Math.abs(diffPercent) <= (avgEatDiffPercent * METRICS_BACK_PERCENT)) {
                    //往回搬;
                    log.info("逆差：{},往回搬！", diffPercent);
                } else {
                    log.info("逆差：{},逆差过大，不搬！", diffPercent);
                    return;
                }
            } else {
                log.info("亏损、方向错误!");
                return;
            }
        } else {
            // 有利润
            if (diffPercent < avgEatDiffPercent) {
                if (coinDiffAfter < coinDiffBefore) { // 方向正确
                    //往回搬;
                    log.info("顺差：{},往回搬！", diffPercent);
                } else { // 方向错误
                    log.info("顺差：{},利润太小，不搬！", diffPercent);
                    return;
                }
            }
        }
        double profit = ((moneyAfter - moneyBefore) / moneyBefore) * 100;
        log.info("Profit:{}", profit);
        if (Context.getUnFilledOrderNum() > 0) {
            log.info("！！！！！！！Waiting for fill order num:{}.", Context.getUnFilledOrderNum());
            throw new TradeException("有[" + Context.getUnFilledOrderNum() + "]个交易未完成！");
        }
        log.info("============================ 准备下订单 ============================");
        // 统一精度4
        minTradeVolume = round(minTradeVolume);

        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            orderService.createOrder(buyOrder, marketA, pair);
            orderService.createOrder(sellOrder, marketB, pair);
        } else {  // 市场B买. 市场A卖
            orderService.createOrder(sellOrder, marketA, pair);
            orderService.createOrder(buyOrder, marketB, pair);
        }
        log.info("Done!");
        // 跟踪买卖订单，准备下次买卖；
    }

    private static double round(double d) {
        DecimalFormat df = new DecimalFormat("#.####");
        return Double.parseDouble(df.format(d));
    }

    public static void main(String[] args) {
        double d = 0.1938439892950703;
        System.out.println(round(d));
    }
}
