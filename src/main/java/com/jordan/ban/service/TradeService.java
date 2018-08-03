package com.jordan.ban.service;

import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Order;
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
    public static final double MIN_TRADE_AMOUNT = 0.000001;

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

    public synchronized void trade(MockTradeResultIndex tradeResult) {

//        log.info("Data:" + tradeResult.toString());

        this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());

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

//        log.info("account A: market={}, money={},coin={}", accountA.getPlatform(), accountA.getMoney(), accountA.getVirtualCurrency());
//        log.info("account B: market={}, money={},coin={}", accountB.getPlatform(), accountB.getMoney(), accountB.getVirtualCurrency());

//        log.info("Mock account!");
//        accountB = AccountDto.builder().money(96.88 * 10).platform(Fcoin.PLATFORM_NAME).symbol(symbol).virtualCurrency(10).build();
        /*
        AccountDto accountA = AccountDto.builder().money(96.88 * 10).platform(Huobi.PLATFORM_NAME).symbol(symbol).virtualCurrency(10).build();
        */

        if (accountA == null || accountB == null) {
            throw new TradeException("Load account error！");
        }
        double coinDiffBefore = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
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
            return;
        }
        if (minTradeVolume <= MIN_TRADE_AMOUNT) {
//            log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
            return;
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
            return;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
//            log.info("Coin is not enough！!");
            return;
        }
        Double avgEatDiffPercent = tradeCounter.getSuggestDiffPercent();
        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();

//        log.info("tradeVolume={}, diffPercent={}, moveMetrics={}, moveBackMetrics={}",
//                minTradeVolume, diffPercent, this.tradeContext.getMoveMetrics(), this.tradeContext.getMoveBackMetrics());

        if (diffPercent < 0) {  // 亏损
            if (coinDiffAfter < coinDiffBefore) { // 币的流动方向正确
                if (Math.abs(diffPercent) <= (avgEatDiffPercent * tradeContext.getMoveBackMetrics())) {
                    //往回搬;
//                    log.info("+++++++diffPercent:{},move back!", diffPercent);
                } else {
//                    log.info("-------diffPercent:{},not deal!", diffPercent);
                    return;
                }
            } else {
//                log.info("--------diffPercent:{},not deal!", diffPercent);
                return;
            }
        } else {
            // 有利润
            if (diffPercent < avgEatDiffPercent) {
                if (coinDiffAfter < coinDiffBefore) { // 币的流动方向正确
                    //往回搬;
//                    log.info("++++++++++++++diffPercent:{},move back!", diffPercent);
                } else { // 方向错误
//                    log.info("+++++diffPercent:{},less than {} .not deal!", diffPercent, avgEatDiffPercent);
                    return;
                }
            }
        }
        double profit = ((moneyAfter - moneyBefore) / moneyBefore) * 100;
//        log.info("Profit:{}", profit);
        /*if (Context.getUnFilledOrderNum() > 0) {
            log.info("！！！！！！！Waiting for fill order num:{}.", Context.getUnFilledOrderNum());
            throw new TradeException("sum[" + Context.getUnFilledOrderNum() + "]wait for deal!!!!");
        }*/
        log.info("============================ PLACE ORDER ============================");
        long start = System.currentTimeMillis();
        // 统一精度4
        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            double fees = 1 + FeeUtils.getFee(marketA.getName());
            buyOrder.setAmount(buyOrder.getAmount() * fees * fees);
            orderService.createOrder(buyOrder, marketA, pair, tradeResult.getTradeDirect(), diffPercent);
            orderService.createOrder(sellOrder, marketB, pair, tradeResult.getTradeDirect(), diffPercent);
        } else {  // 市场B买. 市场A卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            double fees = 1 + FeeUtils.getFee(marketB.getName());
            buyOrder.setAmount(buyOrder.getAmount() * fees * fees);
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
    }

    public static void main(String[] args) {
//        buyOrder.getAmount() * (1 + FeeUtils.getFee(marketA.getName()

        double amount = 10;
        double fees = 0.002;

        double addFees = 1 + fees;
        System.out.println(amount * addFees * addFees);
    }
}
