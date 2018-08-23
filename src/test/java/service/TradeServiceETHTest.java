package service;


import com.jordan.ban.TradeApplication;
import com.jordan.ban.dao.GridRepository;
import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.MockTradeResultIndex;
import com.jordan.ban.domain.OrderState;
import com.jordan.ban.domain.OrderType;
import com.jordan.ban.entity.Order;
import com.jordan.ban.market.parser.*;
import com.jordan.ban.service.GridService;
import com.jordan.ban.service.OrderService;
import com.jordan.ban.service.TradeServiceETH;
import com.jordan.ban.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jordan.ban.common.Constant.ETH_USDT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TradeApplication.class)
@Slf4j
public class TradeServiceETHTest {

    @Autowired
    private TradeServiceETH tradeService;

    @Autowired
    private OrderService orderService;
    @Autowired
    private GridService gridService;

    @Autowired
    private GridRepository gridRepository;

    private AccountDto accountA;
    private AccountDto accountB;
    private MarketParser marketA;
    private MarketParser marketB;

    @Test
    public void queryOrder() {
        Huobi huobi = (Huobi) MarketFactory.getMarket("Huobi");
        huobi.getAccounts();
    }

    @Test
    public void createOrder() {
        Order order = Order.builder().state(OrderState.submitted).amount(2).price(1.2).type(OrderType.SELL_LIMIT).platform("Huobi")
                .orderId("").build();
        orderService.refreshOrderState(order);
    }

    @Test
    public void testTrade() {
        String json = "{\"tradeVolume\":9.8,\"buyPrice\":98.39,\"platformB\":\"Fcoin\",\"symbol\":\"LTCUSDT\",\"buyCost\":71752.00071129,\"platformA\":\"Huobi\",\"eatDiff\":-0.40356000000000514,\"sellCost\":71458.30207982,\"diffPlatform\":\"Huobi-Fcoin\",\"eatTradeVolume\":727.8055,\"sellPrice\":98.38,\"tradeDiff\":-0.35355999999999377,\"money\":\"USDT\",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"A2B\",\"currency\":\"LTC\",\"eatPercent\":-0.0041016363451570805,\"tradePercent\":-0.003593454619371824}";
        this.tradeService.preTrade(JSONUtil.getEntity(json, MockTradeResultIndex.class));
    }


    @Before
    public void before() {
//        this.initGrid();
        this.initAccount();
        this.initMarket();
        this.tradeService.setSymbol(ETH_USDT);
    }


    private void initGrid() {
        log.info("init grid..");
        this.gridRepository.deleteAll();
        final double totalCoin = 5; // todo
        gridService.initGrid(ETH_USDT, -0.005f, 0f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, -0.01f, -0.005f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.01f, 0.015f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.015f, 0.02f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0f, 0.005f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.005f, 0.01f, 0.2f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.02f, 0.025f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.025f, 0.03f, 0.1f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.03f, 0.04f, 0.05f, totalCoin);
        gridService.initGrid(ETH_USDT, 0.04f, 1f, 0.05f, totalCoin);
    }


    private void initAccount() {
        log.info("init account..");
        this.accountA = AccountDto.builder().money(0).platform(Dragonex.PLATFORM_NAME).symbol(ETH_USDT).virtualCurrency(5).build();
        this.accountB = AccountDto.builder().money(1500).platform(Fcoin.PLATFORM_NAME).symbol(ETH_USDT).virtualCurrency(0).build();
        tradeService.setAccountA(accountA);
        tradeService.setAccountB(accountB);
    }

    private void initMarket() {
        log.info("init market..");
        this.marketA = MarketFactory.getMarket(Dragonex.PLATFORM_NAME);
        this.marketB = MarketFactory.getMarket(Fcoin.PLATFORM_NAME);
        tradeService.setMarketA(marketA);
        tradeService.setMarketB(marketB);
    }

    private void initWarehouse() {

    }


    @Test
    public void testTradeRollback() {

        String source = "{\"tradeVolume\":0.1,\"buyPrice\":271,\"platformB\":\"Fcoin\",\"symbol\":\"ETHUSDT\",\"buyCost\":27.1," +
                "\"platformA\":\"Dragonex\",\"eatDiff\":0,\"sellCost\":28.1," +
                "\"diffPlatform\":\"Dragonex-Fcoin\",\"eatTradeVolume\":0.1,\"sellPrice\":281,\"tradeDiff\":1,\"money\":\"USDT\"" +
                ",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"B2A\",\"currency\":\"ETH\",\"eatPercent\":0.002,\"tradePercent\":0}";
        this.tradeService.trade(JSONUtil.getEntity(source, MockTradeResultIndex.class));
    }

    @Test
    public void testWarehouseOut() {
        String source1 = "{\"tradeVolume\":0.1,\"buyPrice\":271,\"platformB\":\"Fcoin\",\"symbol\":\"ETHUSDT\",\"buyCost\":27.1," +
                "\"platformA\":\"Dragonex\",\"eatDiff\":0,\"sellCost\":28.1," +
                "\"diffPlatform\":\"Dragonex-Fcoin\",\"eatTradeVolume\":0.1,\"sellPrice\":281,\"tradeDiff\":1,\"money\":\"USDT\"" +
                ",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"B2A\",\"currency\":\"ETH\",\"eatPercent\":0.002,\"tradePercent\":0}";
        this.tradeService.trade(JSONUtil.getEntity(source1, MockTradeResultIndex.class));

        String source = "{\"tradeVolume\":0.1,\"buyPrice\":271,\"platformB\":\"Fcoin\",\"symbol\":\"ETHUSDT\",\"buyCost\":27.1," +
                "\"platformA\":\"Dragonex\",\"eatDiff\":0,\"sellCost\":28.1," +
                "\"diffPlatform\":\"Dragonex-Fcoin\",\"eatTradeVolume\":0.1,\"sellPrice\":281,\"tradeDiff\":1,\"money\":\"USDT\"" +
                ",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"A2B\",\"currency\":\"ETH\",\"eatPercent\":0.0041,\"tradePercent\":0}";
        this.tradeService.trade(JSONUtil.getEntity(source, MockTradeResultIndex.class));
    }

    @Test
    public void testWarehouseOutRollback() {
        accountA.setMoney(281);
        accountB.setVirtualCurrency(1);
        String source = "{\"tradeVolume\":0.1,\"buyPrice\":271,\"platformB\":\"Fcoin\",\"symbol\":\"ETHUSDT\",\"buyCost\":27.1," +
                "\"platformA\":\"Dragonex\",\"eatDiff\":0,\"sellCost\":28.1," +
                "\"diffPlatform\":\"Dragonex-Fcoin\",\"eatTradeVolume\":0.3,\"sellPrice\":281,\"tradeDiff\":1,\"money\":\"USDT\"" +
                ",\"createTime\":1529375290869,\"costTime\":947,\"tradeDirect\":\"A2B\",\"currency\":\"ETH\",\"eatPercent\":0.0041,\"tradePercent\":0}";
        this.tradeService.trade(JSONUtil.getEntity(source, MockTradeResultIndex.class));
    }
}
