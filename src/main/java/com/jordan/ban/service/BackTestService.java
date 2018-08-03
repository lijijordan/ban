package com.jordan.ban.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordan.ban.dao.*;
import com.jordan.ban.domain.*;
import com.jordan.ban.domain.Policy;
import com.jordan.ban.entity.*;
import com.jordan.ban.es.ElasticSearchClient;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.FeeUtils;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.TradeCounter;
import com.jordan.ban.market.parser.*;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.jordan.ban.common.Constant.*;
import static com.jordan.ban.market.trade.TradeHelper.TRADE_FEES;
import static com.jordan.ban.service.TradeService.MIN_TRADE_AMOUNT;

@Service
@Slf4j
public class BackTestService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TradeCounter tradeCounter;

    @Autowired
    private TradeContext tradeContext;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TradeRecordRepository tradeRecordRepository;

    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;


    @Autowired
    private BackTestStatisticsRepository backTestStatisticsRepository;

    @Autowired
    private TradeRecordService tradeRecordService;

    @Autowired
    private GridRepository gridRepository;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private GridService gridService;

    // unit USDt
    private final static double START_MONEY = 1000;

    private double totalCostMoney;

    private Date start;

    private Date end;

    private long totalData;

    private double totalMoneyBefore;

    private float upPercent;
    private float downPercent;

    private int countProfit;

    private double sumProfit;

    private int avgQueueSize;

    private int split = 1;

    private double wareHouseDiff = 0.006;

    private Grid grid;

    private String symbol;

    public Account getAccount(String platform, String symbol) {
        return this.accountRepository.findBySymbolAndPlatform(symbol, platform);
    }

    private Map<String, Double> lastTicker = new ConcurrentHashMap<>();


    public void preTrade(MockTradeResultIndex tradeResult) {
        if (tradeResult.getTradeVolume() < MIN_TRADE_AMOUNT) {
            log.info("Trade volume:[{}] less than min trade amount.", tradeResult.getTradeVolume());
            return;
        }

        this.trade(tradeResult);
//        if (!this.tradeCounter.isFull()) {
//            log.info("Pool is not ready [{}]", this.tradeCounter.getSize());
//            this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
//        } else {
//            boolean isTrade = this.trade(tradeResult);
//            if (!isTrade) { // 交易的数据不记录到Counter
//                this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
//            }
//        }
    }

    private boolean trade(MockTradeResultIndex tradeResult) {
        String key = tradeResult.getSymbol() + tradeResult.getTradeDirect();
        if (lastTicker.get(key) != null && lastTicker.get(key) == tradeResult.getEatTradeVolume()) {
            return false;
        }

        MarketParser marketA = MarketFactory.getMarket(tradeResult.getPlatformA());
        MarketParser marketB = MarketFactory.getMarket(tradeResult.getPlatformB());

        String symbol = tradeResult.getSymbol().toLowerCase();

        Account accountA = this.getAccount(marketA.getName(), symbol);
        Account accountB = this.getAccount(marketB.getName(), symbol);

        if (accountA == null || accountB == null) {
            throw new TradeException("Load account error！");
        }
        double moneyBefore = accountA.getMoney() + accountB.getMoney();

        // 最小交易量
        double minTradeVolume = tradeResult.getEatTradeVolume();
        double buyPrice = tradeResult.getBuyPrice();
        double sellPrice = tradeResult.getSellPrice();
        double canBuyCoin;
        double diffPercent = tradeResult.getEatPercent();
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
        //- [ ] 【策略】为了能捕获到更多的“高点”，而不是一次就梭哈了，导致后面有行情也做不了事情了——采用分批次搬的策略：每次搬可搬总量的1/4
        if ((minTradeVolume / split) > MIN_TRADE_AMOUNT) {
            minTradeVolume = minTradeVolume / split;
        }

        // 正向匹配网格
        if (TradeDirect.B2A == tradeResult.getTradeDirect()) {
            minTradeVolume = this.gridService.matchGrid(diffPercent, minTradeVolume, this.grid, this.symbol);
            log.info("match grid volume:{}", minTradeVolume);
            if (minTradeVolume == 0) {
                log.info("trade volume：{} less than min trade volume，not deal！", minTradeVolume);
                return false;
            }
        }
        // 逆向出仓
        if (TradeDirect.A2B == tradeResult.getTradeDirect()) {
            // 检查仓位，准备出库
            minTradeVolume = this.warehouseService.checkAndOutWareHouse(tradeResult.getEatPercent(), minTradeVolume);
            if (minTradeVolume == 0) {
                log.info("Not any assets!");
                return false;
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
//            throw new TradeException("Not enough money.");
            return false;
        }
        if (accountA.getVirtualCurrency() < 0 || accountB.getVirtualCurrency() < 0) {
            log.info("Coin is not enough！!");
//            throw new TradeException("Not enough coin.");
            return false;
        }
        double moneyAfter = accountA.getMoney() + accountB.getMoney();

        double profit = moneyAfter - moneyBefore;
        log.info("============================ PLACE ORDER ============================");
        OrderRequest buyOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(buyPrice).symbol(symbol).type(OrderType.BUY_LIMIT).build();
        OrderRequest sellOrder = OrderRequest.builder().amount(minTradeVolume)
                .price(sellPrice).symbol(symbol).type(OrderType.SELL_LIMIT).build();
        log.info("Place order, buy:" + buyOrder + "sell:" + sellOrder);
        String pair = UUID.randomUUID().toString();
        if (tradeResult.getTradeDirect() == TradeDirect.A2B) { // 市场A买. 市场B卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
//            buyOrder.setAmount(buyOrder.getAmount() * (1 + FeeUtils.getFee(marketA.getName())));
            this.placeOrder(accountA, buyOrder);
            this.placeOrder(accountB, sellOrder);
        } else {  // 市场B买. 市场A卖
            // 买入时，为了保持总币量不变，把扣除的手续费部分加入到买单量
//            buyOrder.setAmount(buyOrder.getAmount() * (1 + FeeUtils.getFee(marketB.getName())));
            this.placeOrder(accountA, sellOrder);
            this.placeOrder(accountB, buyOrder);
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
        record.setTotalMoney(totalMoney);
        record.setUpPercent(upPercent);
        record.setDownPercent(downPercent);
        this.tradeRecordRepository.save(record);

        //warehouse
        if (tradeResult.getTradeDirect() == TradeDirect.B2A) {
            // 建仓
            this.warehouseService.buildWareHouse(record, this.wareHouseDiff, this.grid.getID());
        }

        this.lastTicker.put(key, tradeResult.getEatTradeVolume());

        totalCostMoney = totalCostMoney + sellCost + buyCost;
        log.info("Record done!");
        if (profit < 0) {
            this.countProfit += minTradeVolume;
            this.sumProfit += profit;
        }
        return true;
    }


    private void placeOrder(Account account, OrderRequest orderRequest) {
        this.accountRepository.save(account);
    }

    public void init(double price, String symbol) {
        this.tradeRecordRepository.deleteAll();
        this.gridRepository.deleteAll();
        this.profitStatisticsRepository.deleteAll();
        this.initAccountAsARicher(price, symbol);
        Account accountA = this.getAccount(Dragonex.PLATFORM_NAME, symbol);
        Account accountB = this.getAccount(Fcoin.PLATFORM_NAME, symbol);
        this.totalMoneyBefore = accountA.getMoney() + accountB.getMoney();

        this.initGrid(accountA.getVirtualCurrency() + accountB.getVirtualCurrency(), symbol);
    }

    private void initAccountAsARicher(double price, String symbol) {
        this.accountService.emptyAccount();
        Account a = Account.builder().platform(Dragonex.PLATFORM_NAME).symbol(symbol).money(0).virtualCurrency(START_MONEY / price).build();
        Account b = Account.builder().platform(Fcoin.PLATFORM_NAME).symbol(symbol).money(START_MONEY).virtualCurrency(0).build();
        this.accountRepository.save(a);
        this.accountRepository.save(b);

    }


    private void configContext(double backPercent, int queueSize) {
//        TradeCounter.QUEUE_SIZE = 60 * 2 * 30 * 2;
        this.tradeContext.setMoveBackMetrics(backPercent);
        TradeCounter.init(queueSize, avgQueueSize);// avg 2 hour
    }

    private void statistic(String platformA, String platformB, String symbol, int queueSize) {
        Account accountA = this.getAccount(platformA, symbol);
        Account accountB = this.getAccount(platformB, symbol);
        //Balance coin
        log.info("Balance coin again!");
        ProfitStatistics before = profitStatisticsRepository.findTopBySymbolAndAndPlatformAAndAndPlatformBOrderByCreateTimeDesc(symbol, Huobi.PLATFORM_NAME, Fcoin.PLATFORM_NAME);
        double moneyBefore, increase, increasePercent, moneyAfter, coinAfter;
        moneyAfter = accountA.getMoney() + accountB.getMoney();
        coinAfter = accountA.getVirtualCurrency() + accountB.getVirtualCurrency();
        if (before != null) {
            moneyBefore = before.getSumMoney();
        } else {
            moneyBefore = moneyAfter;
        }
        increase = moneyAfter - moneyBefore;
        increasePercent = (increase / Math.min(moneyAfter, moneyBefore)) * 100;

        StatisticRecordDto statisticRecordDto = this.tradeRecordService.queryAndStatisticTradeRecord();
        ProfitStatistics after;
        if (statisticRecordDto != null) {
            after = ProfitStatistics.builder().cycleType(CycleType.day).symbol(symbol)
                    .increase(increase).sumCoin(coinAfter).sumMoney(moneyAfter)
                    .increasePercent(increasePercent).platformA(accountA.getPlatform())
                    .sumCostMoney(statisticRecordDto.getSumCostMoney())
                    .sumProfit(statisticRecordDto.getSumProfit())
                    .avgA2BDiffPercent(statisticRecordDto.getAvgA2BDiffPercent())
                    .avgB2ADiffPercent(statisticRecordDto.getAvgB2ADiffPercent())
                    .sumA2BProfit(statisticRecordDto.getSumA2BProfit())
                    .sumB2AProfit(statisticRecordDto.getSumB2AProfit())
                    .avgA2BProfit(statisticRecordDto.getAvgA2BProfit())
                    .avgB2AProfit(statisticRecordDto.getAvgB2AProfit())
                    .platformB(accountB.getPlatform()).build();
        } else {
            after = ProfitStatistics.builder().cycleType(CycleType.day).symbol(symbol)
                    .increase(increase).sumCoin(coinAfter).sumMoney(moneyAfter)
                    .increasePercent(increasePercent).platformA(accountA.getPlatform())
                    .platformB(accountB.getPlatform()).build();
        }
        this.profitStatisticsRepository.save(after);
        // 恢复初始币量：
        double leftCoin = this.getAccount(Fcoin.PLATFORM_NAME, symbol).getVirtualCurrency();
        double sellMoney = leftCoin * statisticRecordDto.getAvgA2BProfit();
        log.info("left money:{}", sellMoney);
        double profit = this.totalCostMoney * 0.001 + (moneyAfter - this.totalMoneyBefore) + sellMoney;

        BackTestStatistics backTestStatistics = BackTestStatistics.builder().totalCostMoney(totalCostMoney).platformA(platformA)
                .platformB(platformB).sumCoin(coinAfter).sumMoney(moneyAfter).metricsBackPercent(this.tradeContext.getMoveBackMetrics())
                .start(start).end(end).tradeCount(this.tradeRecordRepository.countBy())
                .profit(profit).total(totalData)
                .queueSize(queueSize).symbol(symbol).upPercent(this.upPercent).downPercent(this.downPercent)
                .build();
        backTestStatisticsRepository.save(backTestStatistics);
    }

    public void run(String start, String end, int maxQueueSize, String symbol) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        this.run(format.parse(start), format.parse(end), -1, maxQueueSize, symbol);
    }

    private void run(Date start, Date end, double percent, int queueSize, String symbol) throws ParseException {
        this.start = start;
        this.end = end;
        this.configContext(percent, queueSize);
        MatchPhraseQueryBuilder mpq1 = QueryBuilders.matchPhraseQuery("diffPlatform", "Dragonex-Fcoin");
        MatchPhraseQueryBuilder mpq2 = QueryBuilders.matchPhraseQuery("symbol", symbol);
        QueryBuilder qb2 = QueryBuilders.boolQuery()
                .must(mpq1)
                .must(mpq2);

        SearchResponse scrollResp = ElasticSearchClient.getClient().prepareSearch(MOCK_TRADE_INDEX)
                .addSort("createTime", SortOrder.ASC)
                .setScroll(new TimeValue(60000))
                .setPostFilter(QueryBuilders.rangeQuery("createTime").from(start.getTime()).to(end.getTime()))
                .setQuery(qb2)
                .setSize(1000).get(); //max of 100 hits will be returned for each scroll
        int i = 0, sum = 1;
        //Scroll until no hits are returned
        float total = scrollResp.getHits().getTotalHits();
        this.totalData = (long) total;
        do {
            log.info("Total={}, i={}", total, i++);
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                Map<String, Object> map = hit.getSourceAsMap();
                ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                MockTradeResultIndex index = mapper.convertValue(map, MockTradeResultIndex.class);
                if (index.getSymbol().equals(symbol)) {
                    if (sum == 1) {
                        this.init(index.getBuyPrice(), symbol);
                    }
                    this.preTrade(index);
                    System.out.println("::::::::::::::::::::::::sum::::::::::::::::::" + sum++);
                    System.out.printf("Percentage In Exam: %.1f%%%n", sum / total * 100);
                }
            }
            scrollResp = ElasticSearchClient.getClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        this.statistic(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, symbol, queueSize);
    }

    public void initGrid(double totalCoin, String symbol) {
        this.symbol = symbol;
        gridService.initGrid(symbol, 0.02f, 0.03f, 0.05f, totalCoin);
        gridService.initGrid(symbol, 0.03f, 0.04f, 0.2f, totalCoin);
        gridService.initGrid(symbol, 0.04f, 0.05f, 0.3f, totalCoin);
        gridService.initGrid(symbol, 0.05f, 0.06f, 0.3f, totalCoin);
        gridService.initGrid(symbol, 0.06f, 1f, 0.15f, totalCoin);
    }





}




