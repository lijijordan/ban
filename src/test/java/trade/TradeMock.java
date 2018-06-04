package trade;

import com.jordan.ban.ConsumerApplication;
import com.jordan.ban.domain.Account;
import com.jordan.ban.domain.DifferAskBid;
import com.jordan.ban.domain.MarketDepth;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import org.junit.Before;
import org.junit.Test;

/**
 * /    / 1、虚拟账户
 * // 2、监控行情
 * // 3、执行买卖
 */
public class TradeMock {


    @Test
    public void testTrade() {
        ConsumerApplication application = new ConsumerApplication();
        application.initAccount();
//        53.28	1.068	52.863	0.08	0.782%
        DifferAskBid diff = new DifferAskBid();
        diff.setDiffer(0.00782f);
        diff.setSymbol("NEOUSDT");

        application.mockTrade(diff);
    }

    @Test
    public void testTradeBack() {
        double d1ask = 11.9751;
        double d1bid = 11.9746;
        double d2ask = 12.075;
        double d2bid = 12.0758;
        System.out.println(String.format("symbol:%s     " +
                        "d1_ask:%s, d1_bid:%s, d2_ask:%s, d2_bid:%s",
                "", d1ask, d1bid, d2ask, d2bid));
        // market1 low. market2 high then market2 sell, market1 buy
        System.out.println(String.format("%s-%s=%s", d1ask, d2bid, (d1ask - d2bid)));
        if (d1ask >= d2bid) { // can deal
            System.out.println(">>>>>>>>>>" + (d1ask - d2bid));
        }
        // market2 low. market2 low then market 1 sell, market2 buy
        System.out.println(String.format("%s-%s=%s", d2ask, d1bid, (d2ask - d1bid)));
        if (d2ask >= d1bid) { // can deal
            System.out.println("<<<<<<<<<<" + (d2ask - d1bid));
        }
    }


    @Test
    public void tradeAB() {
        MarketDepth marketDepth = new MarketDepth();
        marketDepth.setD1ask(57.82);
        marketDepth.setD1bid(57.72);
        marketDepth.setD2ask(57.92);
        marketDepth.setD2bid(57.82);
        this.tradeA2B(marketDepth);
        this.tradeB2A(marketDepth);
    }

    public void tradeA2B(MarketDepth marketDepth) {
        double minVolume = 1;
        double tradeBuy = marketDepth.getD1ask() * minVolume;
        double tradeSell = marketDepth.getD2bid() * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
        System.out.println("Market 1 buy , Market 2 sell. diff=" + (float)tradeDiff);
    }

    public void tradeB2A(MarketDepth marketDepth) {
        double minVolume = 1;
        double tradeBuy = marketDepth.getD2ask() * minVolume;
        double tradeSell = marketDepth.getD1bid() * minVolume;
        double tradeDiff = tradeSell - tradeBuy;
        System.out.println("Market 1 sell , Market 2 buy. diff=" + (float)tradeDiff);
    }


}
