package com.jordan.ban.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordan.ban.dao.AccountRepository;
import com.jordan.ban.dao.BackTestStatisticsRepository;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.dao.TradeRecordRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Account;
import com.jordan.ban.entity.BackTestStatistics;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.entity.TradeRecord;
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

    // unit USDt
    private final static double START_MONEY = 1000;

    private double totalCostMoney;

    private Date start;

    private Date end;

    private long totalData;

    private double openPrice;

    private double marketASellPrice;
    private double marketABuyPrice;

    private double marketBSellPrice;
    private double marketBBuyPrice;


    private double moveMetric;

    private double totalMoneyBefore;

    private float upPercent;
    private float downPercent;

    private Policy policy;

    private int countProfit;

    private double sumProfit;

    public Account getAccount(String platform, String symbol) {
        return this.accountRepository.findBySymbolAndPlatform(symbol, platform);
    }

    private Map<String, Double> lastTicker = new ConcurrentHashMap<>();


    public void beforeTrade(MockTradeResultIndex tradeResult) {
        if (this.policy == Policy.max && !this.tradeCounter.isFull()) {
            log.info("Pool is not ready [{}]", this.tradeCounter.getSize());
            this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
        } else {
            boolean isTrade = this.trade(tradeResult);
            if (!isTrade) { // 交易的数据不记录到Counter
                this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
            }
        }
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
//        log.info("account A: market={}, money={},coin={}", accountA.getPlatform(), accountA.getMoney(), accountA.getVirtualCurrency());
//        log.info("account B: market={}, money={},coin={}", accountB.getPlatform(), accountB.getMoney(), accountB.getVirtualCurrency());
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
//            log.info("trade volume is 0!");
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
//
//  Double avgEatDiffPercent = tradeCounter.getSuggestDiffPercent();
        double avgEatDiffPercent;
        if (this.moveMetric < 0) {
            avgEatDiffPercent = tradeCounter.getSuggestDiffPercent();
        } else {
            avgEatDiffPercent = this.moveMetric;
        }

        double coinDiffAfter = Math.abs(accountA.getVirtualCurrency() - accountB.getVirtualCurrency());
        double moneyAfter = accountA.getMoney() + accountB.getMoney();
        double diffPercent = tradeResult.getEatPercent();

        double upDiffPercent = 1;
        double downDiffPercent = 1;

        if (policy == Policy.max) {
            // 上区间的平均值
            upDiffPercent = tradeCounter.getMaxDiffPercent(true);
            // 下区间的平均值
            downDiffPercent = tradeCounter.getMaxDiffPercent(false);

        }
//        log.info("tradeVolume={}, diffPercent={}, moveMetrics={}, moveBackMetrics={}",
//                minTradeVolume, diffPercent, this.tradeContext.getMoveMetrics(), this.tradeContext.getMoveBackMetrics());
        double downPoint = downDiffPercent * this.downPercent;
        double upPoint = upDiffPercent * this.upPercent;
        log.info("downPoint={},upPoint={}", downPoint, upPoint);

        // Validate
        if (TradeDirect.A2B == tradeResult.getTradeDirect()) {
            if (diffPercent < downPoint) {
                return false;
            }
        } else {
            if (diffPercent < upPoint) {
                return false;
            }
        }


        double profit = moneyAfter - moneyBefore;
        log.info("Profit:{}", profit);
        /*if (Context.getUnFilledOrderNum() > 0) {
            log.info("！！！！！！！Waiting for fill order num:{}.", Context.getUnFilledOrderNum());
            throw new TradeException("sum[" + Context.getUnFilledOrderNum() + "]wait for deal!!!!");
        }*/
        log.info("============================ PLACE ORDER ============================");
        // 统一精度4
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
        record.setVolume(tradeResult.getEatTradeVolume());
        record.setProfit(profit);
        record.setTotalMoney(totalMoney);
        this.tradeRecordRepository.save(record);
        this.lastTicker.put(key, tradeResult.getEatTradeVolume());

        totalCostMoney = totalCostMoney + sellCost + buyCost;
        log.info("Record done!");
        if (profit > 0) {
            this.countProfit++;
            this.sumProfit += profit;
        }
        return true;
    }

    private void placeOrder(Account account, OrderRequest orderRequest) {
        // 买币
        /*if (orderRequest.getType() == OrderType.BUY_LIMIT) {
            account.setVirtualCurrency(account.getVirtualCurrency() + orderRequest.getAmount());
            account.setMoney(account.getMoney() - (orderRequest.getAmount() * orderRequest.getPrice() * (1 + TRADE_FEES)));
        } else {
            account.setVirtualCurrency(account.getVirtualCurrency() - orderRequest.getAmount());
            account.setMoney(account.getMoney() + (orderRequest.getAmount() * orderRequest.getPrice() * (1 - TRADE_FEES)));
        }*/
        this.accountRepository.save(account);
    }

    public void init(double price, String symbol) {
        this.openPrice = price;
        this.tradeRecordRepository.deleteAll();
        this.profitStatisticsRepository.deleteAll();
//        accountService.initAccount(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, symbol, price);
        this.initAccountAsAPoor(price, symbol);
        Account accountA = this.getAccount(Dragonex.PLATFORM_NAME, symbol);
        Account accountB = this.getAccount(Fcoin.PLATFORM_NAME, symbol);
        this.totalMoneyBefore = accountA.getMoney() + accountB.getMoney();
    }

    // 以一个被动的形势开具
    private void initAccountAsAPoor(double price, String symbol) {
        this.accountService.emptyAccount();
        Account a = Account.builder().platform(Fcoin.PLATFORM_NAME).symbol(symbol).money(0).virtualCurrency(START_MONEY / price).build();
        Account b = Account.builder().platform(Dragonex.PLATFORM_NAME).symbol(symbol).money(START_MONEY).virtualCurrency(0).build();
        this.accountRepository.save(a);
        this.accountRepository.save(b);
    }


    private void configContext(double backPercent, int queueSize) {
//        TradeCounter.QUEUE_SIZE = 60 * 2 * 30 * 2;
        this.tradeContext.setMoveBackMetrics(backPercent);
        TradeCounter.setQueueSize(queueSize);
    }

    private void statistic(String platformA, String platformB, String symbol, int queueSize) {
        Account accountA = this.getAccount(platformA, symbol);
        Account accountB = this.getAccount(platformB, symbol);

        //Balance coin
        log.info("Balance coin again!");
        /*double coinA = accountA.getVirtualCurrency();
        double moneyA = accountA.getMoney();
        double totalCoin = accountA.getVirtualCurrency() + accountB.getVirtualCurrency();
        if (coinA > totalCoin / 2) { // sell coin
            double diffCoin = coinA - (totalCoin / 2);
            accountA.setMoney(moneyA + diffCoin * marketABuyPrice);
        } else { // buy
            double diffCoin = (totalCoin / 2) - coinA;
            accountA.setMoney(moneyA - diffCoin * marketASellPrice);
        }
        accountA.setVirtualCurrency(totalCoin / 2);

        double coinB = accountB.getVirtualCurrency();
        double moneyB = accountB.getMoney();
        if (coinB > totalCoin / 2) { // sell coin
            double diffCoin = coinB - (totalCoin / 2);
            accountB.setMoney(moneyB + diffCoin * marketBBuyPrice);
        } else { // buy
            double diffCoin = (totalCoin / 2) - coinB;
            accountB.setMoney(moneyB - diffCoin * marketBSellPrice);
        }
        accountB.setVirtualCurrency(totalCoin / 2);*/

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
        ProfitStatistics after = ProfitStatistics.builder().cycleType(CycleType.day).symbol(symbol)
                .increase(increase).sumCoin(coinAfter).sumMoney(moneyAfter)
                .increasePercent(increasePercent).platformA(accountA.getPlatform())
                .platformB(accountB.getPlatform()).build();
        this.profitStatisticsRepository.save(after);


        // 恢复初始币量：
        double leftCoin = this.getAccount(Dragonex.PLATFORM_NAME, symbol).getVirtualCurrency();
        double sellMoney = leftCoin * this.sumProfit / this.countProfit;
        log.info("left money:{}", sellMoney);
        double profit = this.totalCostMoney * 0.001 + moneyAfter - this.totalMoneyBefore + sellMoney;

        BackTestStatistics backTestStatistics = BackTestStatistics.builder().totalCostMoney(totalCostMoney).platformA(platformA)
                .platformB(platformB).sumCoin(coinAfter).sumMoney(moneyAfter).metricsBackPercent(this.tradeContext.getMoveBackMetrics())
                .start(start).end(end).tradeCount(this.tradeRecordRepository.countBy())
                .profit(profit).total(totalData)
                .queueSize(queueSize).symbol(symbol).upPercent(this.upPercent).downPercent(this.downPercent)
                .build();
        backTestStatisticsRepository.save(backTestStatistics);
    }

    public void run() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date start = format.parse("2018/07/01 00:00:29");
        Date end = format.parse("2018/07/04 23:59:00");
        int defaultQueueSize = 6000; // one hour;

//        this.moveMetric = 0.02692;
//        this.run(start, end, 0.8, defaultQueueSize * 2);
//        this.run(start, end, 0.8, defaultQueueSize / 2);
//        this.run(start, end, 0.8, defaultQueueSize * 4);
//        this.run(start, end, 0.8, defaultQueueSize * 8);
//        this.run(start, end, 0.9, defaultQueueSize);

//        this.run(start, end, 0.7, defaultQueueSize, BCH_USDT);
//        this.run(start, end, 0.81, defaultQueueSize, BCH_USDT);
//        this.run(start, end, 0.9, defaultQueueSize, BCH_USDT);
//        run(start, end, 0.7, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.9, defaultQueueSize, ETH_USDT);
//        run(start, end, 1, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.9, defaultQueueSize, ETH_USDT);


//        this.moveMetric = 0.0358;
//        run(start, end, 1, defaultQueueSize, BCH_USDT);
//        run(start, end, 0.9, defaultQueueSize, BCH_USDT);
//        this.moveMetric = -1;
//        run(start, end, 0.81, defaultQueueSize, BCH_USDT);
//
//        this.moveMetric = 0.02944;
//        run(start, end, 1, defaultQueueSize, BTC_USDT);
//        this.moveMetric = -1;
//        run(start, end, 0.81, defaultQueueSize, BTC_USDT);

//        this.moveMetric = -1;
//        run(start, end, 0.9, defaultQueueSize, BTC_USDT);
//        run(start, end, 0.81, defaultQueueSize * 4, BTC_USDT);
//        run(start, end, 0.9, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.85, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.8, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.75, defaultQueueSize, ETH_USDT);
//        run(start, end, 0.7, defaultQueueSize, ETH_USDT);
//        run(start, end, 1.4f, 0.7f, defaultQueueSize * 2, ETH_USDT);


//        run(start, end, 0.025f, -0.018f, defaultQueueSize * 2, ETH_USDT);

//        run(start, end, 1.02f, 0.98f, defaultQueueSize, ETH_USDT);

//        run(start, end, 1.05f, 0.95f, defaultQueueSize, ETH_USDT);
//        run(start, end, 1.02f, 0.98f, defaultQueueSize * 2, ETH_USDT);
//        run(start, end, 1.02f, 0.98f, defaultQueueSize * 4, ETH_USDT);

        //
//        run(start, end, 1f, 1f, defaultQueueSize, ETH_USDT);

        this.policy = Policy.fix;
        run(start, end, 0.028f, -0.018f, defaultQueueSize, ETH_USDT);
        this.policy = Policy.max;
        run(start, end, 1.02f, 0.98f, defaultQueueSize, ETH_USDT);

        System.out.println("End at:" + new Date());
    }

    private void run(Date start, Date end, float upPercent, float downPercent, int queueSize, String symbol) throws ParseException {
        this.upPercent = upPercent;
        this.downPercent = downPercent;
        this.run(start, end, -1, queueSize, symbol);
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
                    this.beforeTrade(index);
                    System.out.println("::::::::::::::::::::::::sum::::::::::::::::::" + sum++);
                    System.out.printf("Percentage In Exam: %.1f%%%n", sum / total * 100);
                }
            }
            scrollResp = ElasticSearchClient.getClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        this.statistic(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, symbol, queueSize);


    }

    public static void main(String[] args) {
        float total = 165006, sum = 165006 / 2;
        System.out.printf("Percentage In Exam: %.1f%%%n", sum / total * 100);
    }
}


