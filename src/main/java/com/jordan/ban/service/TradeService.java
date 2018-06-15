package com.jordan.ban.service;

import com.jordan.ban.common.Context;
import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.PlatformRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.market.parser.Huobi;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;

@Service
@Slf4j
public class TradeService {

    private static double DEFAULT_METRICS_MAX = 0.003; // 0.8%

    private static double DEFAULT_MIN_TRADE_VOLUME = 1;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    public synchronized void trade(MockTradeResultIndex tradeResult) {
        log.info("marketA:{}", tradeResult.getPlatformA());
        MarketParser marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        MarketParser marketB = MarketFactory.getMarket(tradeResult.getPlatformB());
        // Market A
        /*AccountDto accountA = AccountDto.builder().money(marketA.getBalance(tradeResult.getMoney()).getBalance()).platform(tradeResult.getPlatformA())
                .symbol(tradeResult.getSymbol()).virtualCurrency(marketA.getBalance(tradeResult.getCurrency()).getBalance()).build();

        AccountDto accountB = AccountDto.builder().money(marketB.getBalance(tradeResult.getMoney()).getBalance()).platform(tradeResult.getPlatformB())
                .symbol(tradeResult.getSymbol()).virtualCurrency(marketB.getBalance(tradeResult.getCurrency()).getBalance()).build();*/

        //FIXME: Mock and test
        log.info("Mock account!");
        AccountDto accountA = AccountDto.builder().money(96.88 * 10).platform(Huobi.PLATFORM_NAME).symbol(tradeResult.getSymbol()).virtualCurrency(10).build();
        AccountDto accountB = AccountDto.builder().money(96.88 * 10).platform(Fcoin.PLATFORM_NAME).symbol(tradeResult.getSymbol()).virtualCurrency(10).build();

        if (accountA == null || accountB == null) {
            log.info("Not found account!");
            return;
        }
        double coinDiffBefore = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyBefore = accountA.getMoney() + accountB.getMoney();
        log.info(tradeResult.toString());
        // 最小交易量
        double minTradeVolume = DEFAULT_MIN_TRADE_VOLUME;
        double minBuyCost = tradeResult.getBuyCost();
        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        String symbol = tradeResult.getSymbol().toLowerCase();

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
            log.info("Not enough money!");
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("Not enough coin!");
            return;
        }
        Double avgEatDiffPercent = DEFAULT_METRICS_MAX;
        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();
        if (diffPercent < 0) {  // 亏损
            if (coinDiffAfter < coinDiffBefore) {
                if (Math.abs(diffPercent) <= avgEatDiffPercent * 0.7) {
                    //往回搬;
                    log.info("Move back!");
                } else {
                    log.info("Right way. but not enough profit. Not deal!");
                    return;
                }
            } else {
                log.info("Coin won't balance! Not Deal!");
                return;
            }
        } else {
            // 有利润
            if (diffPercent < avgEatDiffPercent) {
                if (coinDiffAfter < coinDiffBefore) { // 方向正确
                    //往回搬;
                    log.info("Move back! Earn a little.");
                } else { // 方向错误
                    log.info("Coin won't balance! Not Deal!");
                    return;
                }
            }
        }
        double profit = ((moneyAfter - moneyBefore) / moneyBefore) * 100;
        log.info("Profit:{}", profit);
        log.info("Ready to traded !!!!!!!!!!!");
        if (Context.getUnFilledOrderNum() > 0) {
            log.info("订单待处理！");
            return;
        }
        log.info("============================ ORDER ============================");
        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            orderService.placeOrder(buyOrder, marketA, pair);
            orderService.placeOrder(buyOrder, marketB, pair);

        } else {  // 市场B买. 市场A卖
            orderService.placeOrder(sellOrder, marketA, pair);
            orderService.placeOrder(buyOrder, marketB, pair);
        }
        log.info("Done!");
        // 跟踪买卖订单，准备下次买卖；
    }

}
