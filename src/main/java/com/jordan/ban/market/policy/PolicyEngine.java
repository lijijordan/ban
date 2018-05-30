package com.jordan.ban.market.policy;

import com.jordan.ban.domain.*;
import com.jordan.ban.market.parser.*;
import lombok.extern.java.Log;
import sun.jvm.hotspot.oops.Mark;

import javax.swing.plaf.DesktopPaneUI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Log
public class PolicyEngine {


    private static final double DIFF_RELAY = 0.005;// 0.5%

    private CompletionService<Depth> completionService;
    private ExecutorService executorService;

    private Account huobiAccount;
    private Account dragonexAccount;

    private Huobi huobi;
    private Dragonex dragonex;

    public void initAccount() {
        // init huobi
        huobiAccount = new Account();
        huobiAccount.setId(1);
        // USDT
        huobiAccount.setMoney(530);
        huobiAccount.setVirtualCurrency(10);
        huobiAccount.setPlatform(Huobi.PLATFORM_NAME);
        huobiAccount.setName("huobi");

        // init dragonex
        dragonexAccount = new Account();
        dragonexAccount.setId(2);
        // USDT
        dragonexAccount.setMoney(530);
        dragonexAccount.setVirtualCurrency(10);
        dragonexAccount.setPlatform(Dragonex.PLATFORM_NAME);
        dragonexAccount.setName("dragonex");
    }

    public PolicyEngine() {
        this.initAccount();
        this.huobi = new Huobi();
        this.dragonex = new Dragonex();
        executorService = Executors.newCachedThreadPool();
        completionService = new ExecutorCompletionService(executorService);
    }


    public DifferAskBid analysis(String symbol, MarketParser market1, MarketParser market2) throws InterruptedException, ExecutionException {
        DifferAskBid realDiff = null;
        long start = System.currentTimeMillis();
        // FIXME: use asynchronous
        completionService.submit(getDepth(symbol, market1));
        completionService.submit(getDepth(symbol, market2));

        Depth depth1 = completionService.take().get();
        Depth depth2 = completionService.take().get();

        realDiff = this.analysis(depth1, depth2);
        if (realDiff != null) {
            realDiff.setDiffCostTime(System.currentTimeMillis() - start);
            realDiff.setSymbol(symbol);
            realDiff.setDifferPlatform(depth1.getPlatform() + "-" + depth2.getPlatform());
        }
        return realDiff;
    }

    public Callable<Depth> getDepth(String symbol, MarketParser market) {
        Callable<Depth> task = () -> market.getDepth(symbol);
        return task;
    }

    private DifferAskBid analysis(Depth depth1, Depth depth2) {
        DifferAskBid differAskBid = null;
        double d1ask = depth1.getAsks().get(0).getPrice();
        double d1bid = depth1.getBids().get(0).getPrice();
        double d2ask = depth2.getAsks().get(0).getPrice();
        double d2bid = depth2.getBids().get(0).getPrice();

        log.info(String.format("d1_ask:%s, d1_bid:%s, d2_ask:%s, d2_bid:%s",
                d1ask, d1bid, d2ask, d2bid));
        // market1 low. market2 high then market2 sell, market1 buy
        if (d1ask >= d2bid) {
            return this.analysisAsksBids(depth1.getAsks(), depth2.getBids());
        }
        // market2 low. market2 low then market 1 sell, market2 buy
        if (d2ask >= d1bid) {
            return this.analysisAsksBids(depth2.getAsks(), depth1.getBids());
        }
        return differAskBid;
    }


    /**
     * String market1 = "Huobi";
     * String market2 = "Dragonex";
     *
     * @param differ
     */
    public DifferAskBid analysis(Differ differ) {
        float differValue = differ.getDiffer();
        String symbol = differ.getSymbol();
        DifferAskBid realDiff = null;
        long start = System.currentTimeMillis();
        /*String diffMarket = differ.getDifferPlatform();
        String markets[] = diffMarket.split("-");
        String market1, market2;
        if (markets != null) {
            market1 = markets[0];
            market2 = markets[1];
        }*/

        if (Math.abs(differValue) > DIFF_RELAY) {
            // FIXME: use asynchronous
            Depth dragonexDepth = this.dragonex.getDepth(symbol);
            Depth huobiDepth = this.huobi.getDepth(symbol);
            // market1 price bigger than market2 price
            if (differValue > 0) {
                // [huobi] bigger . then do sell. watch buy list.
                List<Ticker> huobiAsks = huobiDepth.getAsks();
                // Dragonex littler . then do buy. watch sell list.
                List<Ticker> dragonexBids = dragonexDepth.getBids();
                realDiff = this.analysisAsksBids(huobiAsks, dragonexBids);
            } else {
                // [Dragonex] bigger . then do sell. watch buy list.
                List<Ticker> huobiBids = huobiDepth.getBids();
                // [Huobi] littler . then do buy. watch sell list.
                List<Ticker> dragonexAsks = dragonexDepth.getAsks();
                realDiff = this.analysisAsksBids(dragonexAsks, huobiBids);
            }
        }

        if (realDiff != null) {
            realDiff.setDiffCostTime(System.currentTimeMillis() - start);
            realDiff.setSymbol(symbol);
            realDiff.setDifferPlatform(differ.getDifferPlatform());
        }
        return realDiff;
    }

    /**
     * ASK : [Ticker(price=12.4938, volume=10.0), Ticker(price=12.4616, volume=9.7877), Ticker(price=12.4477, volume=193.0449), Ticker(price=12.4476, volume=61.6958), Ticker(price=12.4457, volume=18.1787), Ticker(price=12.4428, volume=4.0), Ticker(price=12.4017, volume=69.5823), Ticker(price=12.3907, volume=113.4875), Ticker(price=12.3832, volume=42.1279), Ticker(price=12.376, volume=130.1221)]
     * BID : [Ticker(price=12.4355, volume=196.4697), Ticker(price=12.4353, volume=206.9918), Ticker(price=12.4352, volume=3.0), Ticker(price=12.4351, volume=159.9964), Ticker(price=12.435, volume=238.589), Ticker(price=12.4301, volume=226.6068), Ticker(price=12.43, volume=2437.4485), Ticker(price=12.4299, volume=224.938), Ticker(price=12.4289, volume=1000.0), Ticker(price=12.4261, volume=222.5965), Ticker(price=12.425, volume=96.0), Ticker(price=12.424, volume=779.4563), Ticker(price=12.421, volume=112.0), Ticker(price=12.42, volume=24.6856), Ticker(price=12.4193, volume=766.8), Ticker(price=12.4183, volume=2393.4686), Ticker(price=12.4161, volume=500.0), Ticker(price=12.416, volume=80.0), Ticker(price=12.4159, volume=139.5478), Ticker(price=12.4148, volume=1112.3743)]
     *
     * @param asks
     * @param bids
     */
    private DifferAskBid analysisAsksBids(List<Ticker> asks, List<Ticker> bids) {
        log.info("ASK : " + asks.toString());
        log.info("BID : " + bids.toString());
        DifferAskBid diff = null;

        // validate ask 1 and bid 1
        Ticker ask1 = asks.get(0);
        Ticker bid1 = bids.get(0);

        // read differ
        double realDiffer = (ask1.getPrice() - bid1.getPrice()) / Math.max(ask1.getPrice(), bid1.getPrice());

        // can deal
        if (realDiffer > 0) {
            log.info("real_diff=" + realDiffer * 100 + "%");
            // analysis volume
            diff = new DifferAskBid();
            diff.setAsk1Price(ask1.getPrice());
            diff.setAsk1Volume(ask1.getVolume());
            diff.setBid1Price(bid1.getPrice());
            diff.setBid1Volume(bid1.getVolume());
            diff.setCreateTime(new Date());
//            diffMarket.setDiffCostTime();
            diff.setDiffer((float) realDiffer);
        }
        return diff;
    }
}
