package trade;

import com.jordan.ban.ConsumerApplication;
import com.jordan.ban.domain.Account;
import com.jordan.ban.domain.DifferAskBid;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Huobi;
import org.junit.Before;
import org.junit.Test;

/**
 * /    / 1、虚拟账户
 *     // 2、监控行情
 *     // 3、执行买卖
 */
public class TradeMock {


    @Test
    public void testTrade(){
        ConsumerApplication application = new ConsumerApplication();
        application.initAccount();
//        53.28	1.068	52.863	0.08	0.782%
        DifferAskBid diff = new DifferAskBid();
        diff.setDiffer(0.00782f);
        diff.setSymbol("NEOUSDT");
        diff.setAsk1Price(53.28);
        diff.setAsk1Volume(1.068);
        diff.setBid1Price(52.863);
        diff.setBid1Volume(0.08);

        application.mockTrade(diff);
    }


}
