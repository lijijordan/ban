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
import com.jordan.ban.task.StatisticTask;
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
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.jordan.ban.common.Constant.ETH_USDT;
import static com.jordan.ban.common.Constant.MOCK_TRADE_INDEX;
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

    private double totalCostMoney;

    private Date start;

    private Date end;

    private long totalData;

    private static final double FIX_MOVE_PERCENT = 0.022;


    public Account getAccount(String platform, String symbol) {
        return this.accountRepository.findBySymbolAndPlatform(symbol, platform);
    }

    private Map<String, Double> lastTicker = new ConcurrentHashMap<>();

    public void trade(MockTradeResultIndex tradeResult) {
        String key = tradeResult.getSymbol() + tradeResult.getTradeDirect();
        if (lastTicker.get(key) != null && lastTicker.get(key) == tradeResult.getEatTradeVolume()) {
            return;
        }

        this.tradeCounter.count(tradeResult.getTradeDirect(), tradeResult.getEatPercent());
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
//        Double avgEatDiffPercent = tradeCounter.getSuggestDiffPercent();
        // FIXME: 固定diff 策略
        Double avgEatDiffPercent = FIX_MOVE_PERCENT;


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

    public void init(double price) {
        this.accountService.emptyAccount();
        this.tradeRecordRepository.deleteAll();
        this.profitStatisticsRepository.deleteAll();
        accountService.initAccount(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, ETH_USDT, price);
//        this.statistic(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, ETH_USDT);
    }


    private void configContext(double backPercent) {
//        TradeCounter.QUEUE_SIZE = 60 * 2 * 30 * 2;
        this.tradeContext.setMoveBackMetrics(backPercent);
    }

    private void statistic(String platformA, String platformB, String symbol) {
        Account accountA = this.getAccount(platformA, symbol);
        Account accountB = this.getAccount(platformB, symbol);

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


        BackTestStatistics backTestStatistics = BackTestStatistics.builder().totalCostMoney(totalCostMoney).platformA(platformA)
                .platformB(platformB).sumCoin(coinAfter).sumMoney(moneyAfter).metricsBackPercent(this.tradeContext.getMoveBackMetrics())
                .start(start).end(end).tradeCount(this.tradeRecordRepository.countBy())
                .profit(this.totalCostMoney * 0.001 + moneyAfter - 1000).total(totalData)
                .queueSize(TradeCounter.QUEUE_SIZE)
                .build();
        backTestStatisticsRepository.save(backTestStatistics);
    }

    public void run() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date start = format.parse("2018/06/28 00:00:00");
        Date end = format.parse("2018/06/28 23:59:59");

        this.run(start, end, 0.8);
        /*this.run(start, end, 0.8);
        this.run(start, end, 0.7);
        this.run(start, end, 0.6);
        this.run(start, end, 0.85);
        this.run(start, end, 0.81);
        this.run(start, end, 0.89);*/
    }

    public void run(Date start, Date end, double percent) throws ParseException {
        this.start = start;
        this.end = end;
        this.configContext(percent);

        MatchPhraseQueryBuilder mpq1 = QueryBuilders.matchPhraseQuery("diffPlatform", "Dragonex-Fcoin");
        MatchPhraseQueryBuilder mpq2 = QueryBuilders.matchPhraseQuery("symbol", "ETHUSDT");
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
                if (index.getSymbol().equals(ETH_USDT)) {
                    if (sum == 1) {
                        this.init(index.getBuyPrice());
                    }
                    this.trade(index);
                    System.out.println("::::::::::::::::::::::::sum::::::::::::::::::" + sum++);
                    System.out.printf("Percentage In Exam: %.1f%%%n", sum / total * 100);
                }
            }
            scrollResp = ElasticSearchClient.getClient().prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
        }
        while (scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
        this.statistic(Fcoin.PLATFORM_NAME, Dragonex.PLATFORM_NAME, ETH_USDT);


    }

    public static void main(String[] args) {
        float total = 165006, sum = 165006 / 2;
        System.out.printf("Percentage In Exam: %.1f%%%n", sum / total * 100);
    }
}


