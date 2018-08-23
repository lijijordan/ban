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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;

@Service
@Slf4j
@Data
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

    private AccountDto accountA;
    private AccountDto accountB;

    private MarketParser marketA;
    private MarketParser marketB;

    private double moneyBefore;

    public synchronized boolean preTrade(MockTradeResultIndex tradeResult) {
        this.symbol = tradeResult.getSymbol();
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
            this.initAccount(tradeResult);
            return this.trade(tradeResult);
        } catch (TradeException e) {
            log.info("trade exception:" + e.getMessage());
        }
        return false;
    }

    private void initAccount(MockTradeResultIndex tradeResult) {
        marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        marketB = MarketFactory.getMarket(tradeResult.getPlatformB());
        String symbol = tradeResult.getSymbol().toLowerCase();
        String coinName = symbol.replace("usdt", "");
        accountA = this.getAccount(tradeResult.getPlatformA(), symbol, coinName);
        accountB = this.getAccount(tradeResult.getPlatformB(), symbol, coinName);
        if (accountA == null || accountB == null) {
            throw new TradeException("Load account error！");
        }
        moneyBefore = accountA.getMoney() + accountB.getMoney();
    }

    private AccountDto getAccount(String platName, String symbol, String coinName) {
        MarketParser marketA = MarketFactory.getMarket(platName);
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(marketA.getName());
        AccountDto accountA = AccountDto.builder().money(balanceA.get("usdt").getAvailable()).platform(marketA.getName()).symbol(symbol)
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getAvailable() : 0).build();
        return accountA;
    }
    private double reduceMinTradeVolume(TradeDirect direct, AccountDto accountA,
                                        AccountDto accountB, double eatTradeVolume, double buyPrice, double diffPercent) {
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

        //  ==================== Validate min trade volume ====================
        double minTradeVolume = eatTradeVolume;
        if (minTradeVolume <= 0) {
            log.info("trade volume is 0!");
            throw new TradeException("trade volume is 0");
        }

        // // FIXME:Do not use direct A2B; 正向匹配网格
        if (TradeDirect.B2A == direct) {
            GridMatch gridMatch = this.gridService.matchGrid(diffPercent, minTradeVolume, this.symbol);
            minTradeVolume = gridMatch.getMatchResult();
            this.grid = gridMatch.getGrid();
            log.info("match grid volume:{}", minTradeVolume);
            if (minTradeVolume == 0) {
                throw new TradeException("less than min trade volume，not deal！");
            }
        } else {// 逆向出仓
            // 检查仓位，准备出库
            minTradeVolume = this.warehouseService.checkAndOutWareHouse(diffPercent, minTradeVolume, symbol);
        }

        if (minTradeVolume <= MIN_TRADE_AMOUNT) {
            log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
            throw new TradeException("less than min trade volume，not deal！");
        }
        return minTradeVolume;
    }

    @Transactional(rollbackOn = Exception.class, value = Transactional.TxType.REQUIRES_NEW)
    public boolean trade(MockTradeResultIndex tradeResult) throws TradeException {

        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        double diffPercent = tradeResult.getEatPercent();
        double eatTradeVolume = tradeResult.getEatTradeVolume();
        TradeDirect direct = tradeResult.getTradeDirect();
        double diff = tradeResult.getEatDiff();
        // reduce trade volume
        double minTradeVolume = this.reduceMinTradeVolume(tradeResult.getTradeDirect(), accountA, accountB,
                eatTradeVolume, buyPrice, diffPercent);

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
        long start = System.currentTimeMillis();
        log.info("============================ PLACE ORDER ============================");
        String pair = this.placeOrder(buyPrice, sellPrice, minTradeVolume, direct, diffPercent);
        log.info("================================ DONE ===============================");


        // 记录Record
        TradeRecord record = this.createOrderRecord(direct, diff,
                diffPercent, minTradeVolume, buyCost + sellCost, pair);
        // build warehouse
        if (tradeResult.getTradeDirect() == TradeDirect.B2A) {
            this.warehouseService.buildWareHouse(record,
                    this.tradeContext.getWareHouseDiff(), this.grid.getID(), symbol);
        }
        log.info("Record done! cost time:[{}]s", (System.currentTimeMillis() - start));
        return true;
    }

    private TradeRecord createOrderRecord(TradeDirect direct, double diff, double diffPercent, double minTradeVolume, double totalCost, String pair) {
        log.info("Record trade information:");
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double profit = moneyAfter - moneyBefore;
        double totalMoney = accountA.getMoney() + accountB.getMoney();
        TradeRecord record = new TradeRecord();
        record.setPlatformA(accountA.getPlatform());
        record.setPlatformB(accountB.getPlatform());
        record.setDirect(direct);
        record.setEatDiff(diff);
        record.setEatDiffPercent(diffPercent);
        record.setSymbol(this.symbol);
        record.setTradeTime(new Date());
        record.setVolume(minTradeVolume);
        record.setProfit(profit);
        record.setTradeCostMoney(totalCost);
        record.setTotalMoney(totalMoney);
        record.setDownPercent(tradeContext.getDownPoint());
        record.setUpPercent(tradeContext.getUpPoint());
        record.setOrderPairKey(pair);
        return this.tradeRecordRepository.save(record);
    }


    private String placeOrder(double buyPrice, double sellPrice, double minTradeVolume, TradeDirect direct, double diffPercent) {

        // 统一精度4
        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (direct == TradeDirect.A2B) { // 市场A买. 市场B卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
            /*double fees = 1 - FeeUtils.getFee(marketA.getName());
            buyOrder.setAmount(buyOrder.getAmount() / fees);*/
            try {
                orderService.createOrderAsync(buyOrder, sellOrder, marketA, marketB, pair, direct, diffPercent);
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
                orderService.createOrderAsync(buyOrder, sellOrder, marketB, marketA, pair, direct, diffPercent);
            } catch (Exception e) {
                throw new TradeException("Create order failed！!");
            }
        }
        return pair;
    }



}
