package com.jordan.ban.service;

import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Grid;
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

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;

@Service
@Slf4j
public class TradeServiceETH {

    //min limit order amount 0.01
    public static final double MIN_TRADE_AMOUNT = 0.01;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TradeContext tradeContext;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private GridService gridService;

    private Grid grid;

    private String symbol;

    private TradeCounter tradeCounter;
    private String tradeResultIdMarkA2B = "";
    private String tradeResultIdMarkB2A = "";

    public synchronized boolean preTrade(MockTradeResultIndex tradeResult) {
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) {
            tradeContext.setA2bCurrentPercent(tradeResult.getEatPercent());
            tradeContext.setA2bCurrentVolume(tradeResult.getEatTradeVolume());

            if (tradeResultIdMarkA2B.equals(tradeResult.getId())) {
                log.info("same id A2B :{} ", tradeResult.getId());
                return false;
            }
            tradeResultIdMarkA2B = tradeResult.getId();
        } else {
            tradeContext.setB2aCurrentPercent(tradeResult.getEatPercent());
            tradeContext.setB2aCurrentVolume(tradeResult.getEatTradeVolume());

            if (tradeResultIdMarkB2A.equals(tradeResult.getId())) {
                log.info("same id B2A :{} ", tradeResult.getId());
                return false;
            }
            tradeResultIdMarkB2A = tradeResult.getId();
        }
        this.tradeCounter.setCurrentDiffPercent(tradeResult.getEatPercent());
        this.tradeContext.setCurrentEthPrice(tradeResult.getBuyPrice());
        // 过滤交易数小于最小交易量的数据
        if (tradeResult.getTradeVolume() < MIN_TRADE_AMOUNT) {
            log.info("Trade volume [{}] is less than min volume[{}]", tradeResult.getTradeVolume(), MIN_TRADE_AMOUNT);
            return false;
        }

        try {
            return this.trade(tradeResult);
        } catch (TradeException e) {
            log.info("trade exception:" + e.getMessage());
        }
        return false;
    }

    private AccountDto initAccount(String platName, String symbol, String coinName) {
        MarketParser marketA = MarketFactory.getMarket(platName);
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(marketA.getName());
        AccountDto accountA = AccountDto.builder().money(balanceA.get("usdt").getAvailable()).platform(marketA.getName()).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getAvailable() : 0).build();
        return accountA;
    }
    private double reduceMinTradeVolume(TradeDirect direct, AccountDto accountA, AccountDto accountB, double eatTradeVolume, double buyPrice) {
        //计算最小量
        double canBuyCoin;
        if (direct == TradeDirect.A2B) { // 市场A买. 市场B卖
            // B市场卖出币量
            if (accountB.getVirtualCurrency() < eatTradeVolume) {
                eatTradeVolume = accountB.getVirtualCurrency();
            }
            // 市场A可买入币量
            canBuyCoin = accountA.getMoney() / (buyPrice * (1 + FeeUtils.getFee(accountA.getPlatform())));
        } else {  // 市场B买. 市场A卖
            if (accountA.getVirtualCurrency() < eatTradeVolume) {
                eatTradeVolume = accountA.getVirtualCurrency();
            }
            // 市场B可买入币量
            canBuyCoin = accountB.getMoney() / (buyPrice * (1 + FeeUtils.getFee(accountB.getPlatform())));
        }
        if (canBuyCoin < eatTradeVolume) {
            eatTradeVolume = canBuyCoin;
        }
        return eatTradeVolume;
    }

    @Transactional(rollbackOn = TradeException.class)
    public boolean trade(MockTradeResultIndex tradeResult) throws TradeException {
        this.symbol = tradeResult.getSymbol();
        MarketParser marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        MarketParser marketB = MarketFactory.getMarket(tradeResult.getPlatformB());
        String symbol = tradeResult.getSymbol().toLowerCase();
        String coinName = symbol.replace("usdt", "");
        AccountDto accountA = this.initAccount(tradeResult.getPlatformA(), symbol, coinName);
        AccountDto accountB = this.initAccount(tradeResult.getPlatformB(), symbol, coinName);

        if (accountA == null || accountB == null) {
            throw new TradeException("Load account error！");
        }
        double moneyBefore = accountA.getMoney() + accountB.getMoney();
        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        double diffPercent = tradeResult.getEatPercent();
        double eatTradeVolume = tradeResult.getEatTradeVolume();
        // reduce trade volume
        double minTradeVolume = this.reduceMinTradeVolume(tradeResult.getTradeDirect(), accountA, accountB,
                eatTradeVolume, buyPrice);

        if (minTradeVolume <= 0) {
            log.info("trade volume is 0!");
            throw new TradeException("trade volume is 0");
        }
        if (minTradeVolume <= MIN_TRADE_AMOUNT) {
            log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
            throw new TradeException("less than min trade volume，not deal！");
        }
        // // FIXME:Do not use direct A2B; 正向匹配网格
        if (TradeDirect.B2A == tradeResult.getTradeDirect()) {
            GridMatch gridMatch = this.gridService.matchGrid(diffPercent, minTradeVolume, this.symbol);
            minTradeVolume = gridMatch.getMatchResult();
            this.grid = gridMatch.getGrid();
            log.info("match grid volume:{}", minTradeVolume);
            if (minTradeVolume == 0) {
                log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
                throw new TradeException("less than min trade volume，not deal！");
            }
        }
        // 逆向出仓
        if (TradeDirect.A2B == tradeResult.getTradeDirect()) {
            // 检查仓位，准备出库
            minTradeVolume = this.warehouseService.checkAndOutWareHouse(tradeResult.getEatPercent(), minTradeVolume, symbol);
            if (minTradeVolume == 0) {
                log.info("Not any assets!");
                throw new TradeException("Not any assets!");
            } else {
                log.info("Asserts:[{}] ready to come out!", minTradeVolume);
            }
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
            log.info("Money is not enough！!");
            throw new TradeException("Money is not enough！!");
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("Coin is not enough！!");
            throw new TradeException("Coin is not enough！!");
        }
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
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
            /*double fees = 1 - FeeUtils.getFee(marketA.getName());
            buyOrder.setAmount(buyOrder.getAmount() / fees);*/
            try {
                orderService.createOrderAsync(buyOrder, sellOrder, marketA, marketB, pair, tradeResult.getTradeDirect(), diffPercent);
            } catch (Exception e) {
                throw new TradeException("Create order failed！!");
            }

        } else {  // 市场B买. 市场A卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            /*double fees = 1 - FeeUtils.getFee(marketB.getName());
            buyOrder.setAmount(buyOrder.getAmount() / fees);*/
//            orderService.createOrder(sellOrder, marketA, pair, tradeResult.getTradeDirect(), diffPercent);
//            orderService.createOrder(buyOrder, marketB, pair, tradeResult.getTradeDirect(), diffPercent);
            try {
                orderService.createOrderAsync(buyOrder, sellOrder, marketB, marketA, pair, tradeResult.getTradeDirect(), diffPercent);
            } catch (Exception e) {
                throw new TradeException("Create order failed！!");
            }
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
        record.setVolume(minTradeVolume);
        record.setProfit(profit);
        record.setTradeCostMoney(buyCost + sellCost);
        record.setTotalMoney(totalMoney);
        record.setDownPercent(tradeContext.getDownPoint());
        record.setUpPercent(tradeContext.getUpPoint());
        record.setOrderPairKey(pair);
        this.tradeRecordRepository.save(record);
        // build warehouse
        if (tradeResult.getTradeDirect() == TradeDirect.B2A) {
            this.warehouseService.buildWareHouse(record,
                    this.tradeContext.getWareHouseDiff(), this.grid.getID(), symbol);
        }
        log.info("Record done! cost time:[{}]s", (System.currentTimeMillis() - start));
        return true;
    }



}
