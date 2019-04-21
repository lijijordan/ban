package com.jordan.ban.service;

import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.dao.TradeStatisticsRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.entity.TradeStatistics;
import com.jordan.ban.exception.ApiException;
import com.jordan.ban.exception.ResourceException;
import com.jordan.ban.exception.StatisticException;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.jordan.ban.TradeApplication.PERCENT;
import static com.jordan.ban.TradeApplication.SPLIT_COUNT;
import static com.jordan.ban.common.Constant.ETH_USDT;
import static com.jordan.ban.common.Constant.USDT;

@Slf4j
@Service
public class OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SlackService slackService;

    @Autowired
    private TradeStatisticsRepository tradeStatisticsRepository;

    @Autowired
    private TradeContext tradeContext;

    private static final float TRADE_PRICE_FLOAT = 0.00001f;


    @Autowired
    private ProfitStatisticsRepository profitStatisticsRepository;

    public List<Order> queryOrder(String symbol, Date date) {
        return this.orderRepository.findAllByCreateTime(date);
    }

    /**
     * 获取为完成交易的订单
     *
     * @return
     */
    public List<Order> getUnfilledOrders() {
        return this.orderRepository.findAllUnfilledOrders();
    }


    //    @Transactional(Pro=Propagation.REQUIRED, readOnly=true, noRollbackFor=Exception.class)
//    @Async
    public void refreshOrderState(Order order) {
        log.debug("refreshOrderState");
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        OrderResponse orderResponse = marketParser.getFilledOrder(order.getOrderId());
        if (orderResponse != null) {
            // 判断订单是否变化，如果变化发送通知
            if (isChanged(order, orderResponse)) {
                Date date = order.getCreateTime();
                long costTime = System.currentTimeMillis() - date.getTime();
                String msg = String.format("type=%s, date=%s, amount=%s, filled=%s, price=%s diffPercent=%s",
                        orderResponse.getOrderState(), orderResponse.getCreateTime(), order.getAmount(),
                        orderResponse.getFilledAmount(), orderResponse.getPrice(), order.getDiffPercent() * 100);
                if (orderResponse.getFilledAmount() == order.getAmount()) {
                    msg = msg + ",OKey";
                }
                this.slackService.sendMessage("Order changed:" + "[" + (costTime / 1000) + "]s", msg);
                // 等待5s以后再刷新余额：等待网站更新
                log.debug("wait for 5s! update balance.");
                try {
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.accountService.refreshBalancesCache(order.getPlatform());
            }
            order.setState(orderResponse.getOrderState());
            order.setFillFees(orderResponse.getFillFees());
            order.setFilledAmount(orderResponse.getFilledAmount());
            order.setUpdateTime(new Date());
            order.setSymbol(orderResponse.getSymbol());
            order.setPrice(orderResponse.getPrice());
            order.setFillFees(orderResponse.getFillFees());
            this.orderRepository.save(order);


            // 交易完成：统计交易信息
            if (orderResponse.getOrderState() == OrderState.filled) {
//                this.accountService.refreshBalancesCache(order.getPlatform());
                this.statisticTrade(order);
            }
        }
    }

    /**
     * Watch Single Order State
     *
     * @param order
     */
    public void refreshSingleOrderState(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        OrderResponse orderResponse = marketParser.getFilledOrder(order.getOrderId());
        if (orderResponse == null) {
            log.error("Get order status failed! Order id :{}", order.getOrderId());
        }
        // 判断订单是否变化，如果变化发送通知
        if (isChanged(order, orderResponse)) {
            Date date = order.getCreateTime();
            long costTime = System.currentTimeMillis() - date.getTime();
            String msg = String.format("type=%s, date=%s, amount=%s, filled=%s, price=%s diffPercent=%s",
                    orderResponse.getOrderState(), orderResponse.getCreateTime(), order.getAmount(),
                    orderResponse.getFilledAmount(), orderResponse.getPrice(), order.getDiffPercent() * 100);
            if (orderResponse.getFilledAmount() == order.getAmount()) {
                msg = msg + ",OKey";
            }
            this.slackService.sendMessage("Order changed:" + "[" + (costTime / 1000) + "]s", msg);
        }
        order.setState(orderResponse.getOrderState());
        order.setFillFees(orderResponse.getFillFees());
        order.setFilledAmount(orderResponse.getFilledAmount());
        order.setUpdateTime(new Date());
        order.setSymbol(orderResponse.getSymbol());
        order.setPrice(orderResponse.getPrice());
        order.setFillFees(orderResponse.getFillFees());
        this.orderRepository.save(order);
        // 订单未完成
        if (order.getState() != OrderState.filled) {
            return;
        }
        // 重新下单
        if (order.getType() == OrderType.BUY_LIMIT) {
            this.crateSingleOrder(OrderRequest.builder()
                    .type(OrderType.SELL_LIMIT).amount(order.getAmount()).price(order.getPrice() * (1 + (PERCENT / SPLIT_COUNT)))
                    .symbol(order.getSymbol()).build(), MarketFactory.getMarket(order.getPlatform()));
        } else {
            this.crateSingleOrder(OrderRequest.builder()
                    .type(OrderType.BUY_LIMIT).amount(order.getAmount()).price(order.getPrice() * (1 - (PERCENT / SPLIT_COUNT)))
                    .symbol(order.getSymbol()).build(), MarketFactory.getMarket(order.getPlatform()));
        }
    }

    public synchronized void statisticTrade(Order order) {
        List<Order> list = this.orderRepository.findAllByOrderPairKey(order.getOrderPairKey());
        if (list.isEmpty() || list.size() < 2) {
            throw new StatisticException("Can not found order pair.");
        }
        Order buyOrder, sellOrder;
        if (list.get(0).getType() == OrderType.BUY_LIMIT) {
            buyOrder = list.get(0);
            sellOrder = list.get(1);
        } else {
            buyOrder = list.get(1);
            sellOrder = list.get(0);
        }
        if (buyOrder.getState() != OrderState.filled || sellOrder.getState() != OrderState.filled) {// 有订单未完成
            log.debug("Order :{} waiting for trade, can not statistic.", order);
            return;
        }

        String symbol = buyOrder.getSymbol().toLowerCase();
        String coinName = symbol.replace(USDT.toLowerCase(), "");

        Map<String, BalanceDto> buyBalance = accountService.findBalancesCache(buyOrder.getPlatform());
        AccountDto buyAccount = AccountDto.builder().money(buyBalance.get(USDT.toLowerCase()).getBalance())
                .virtualCurrency(buyBalance.get(coinName).getBalance()).build();
        Map<String, BalanceDto> sellBalance = accountService.findBalancesCache(sellOrder.getPlatform());
        AccountDto sellAccount = AccountDto.builder().money(sellBalance.get(USDT.toLowerCase()).getBalance())
                .virtualCurrency(sellBalance.get(coinName).getBalance()).build();
        TradeStatistics last = tradeStatisticsRepository.findTopByOrderByCreateTimeDesc();
        if (last == null) {
            last = TradeStatistics.builder().build();
        }
        double totalCoin = buyAccount.getVirtualCurrency() + sellAccount.getVirtualCurrency();
        double totalMoney = buyAccount.getMoney() + sellAccount.getMoney();

        double diffCoin = totalCoin - last.getTotalCoin();
        double diffCoinPercent = diffCoin * 100 / totalCoin;

        double diffMoney = totalMoney - last.getTotalMoney();
        double diffMoneyPercent = diffMoney * 100 / totalMoney;

        TradeStatistics
                tradeStatistics = TradeStatistics.builder().buyFees(buyOrder.getFillFees()).buyMarket(buyOrder.getPlatform()).buyOrderId(buyOrder.getOrderId()).buyPrice(buyOrder.getPrice()).buyVolume(buyOrder.getFilledAmount())
                .sellFees(sellOrder.getFillFees()).sellMarket(sellOrder.getPlatform()).sellOrderId(sellOrder.getOrderId()).sellPrice(sellOrder.getPrice()).sellVolume(sellOrder.getAmount()).orderPairKey(order.getOrderPairKey())
                .totalCoin(totalCoin).totalMoney(totalMoney)
                .coinDiff(diffCoin).moneyDiff(diffMoney).coinDiffPercent(diffCoinPercent).moneyDiffPercent(diffMoneyPercent).build();
        this.tradeStatisticsRepository.save(tradeStatistics);
    }

    private boolean isChanged(Order order, OrderResponse orderResponse) {
        boolean r = false;
        if (order.getState() != orderResponse.getOrderState() || order.getFilledAmount() != orderResponse.getFilledAmount()
                || order.getFillFees() != orderResponse.getFillFees()) {
            r = true;
        }
        return r;
    }


    public void cancelOrder(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        if (marketParser.cancelOrder(order.getOrderId())) {
            order.setState(OrderState.canceled);
            order.setUpdateTime(new Date());
            this.orderRepository.save(order);
        }
    }

    private String placeOrder(OrderRequest orderRequest, MarketParser market) {
        String orderId = "";
        int i = 0;
        while (i < tradeContext.getOrderTryTimes()) {
            try {
                orderId = market.placeOrder(orderRequest);
                return orderId;
            } catch (Exception e) {
                try {
                    log.debug("After 2 seconds, try to place order again.");
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            } finally {
                i++;
            }
        }
        return orderId;
    }

    public void createOrderAsync(OrderRequest buyOrder, OrderRequest sellOrder, MarketParser marketA, MarketParser marketB, String pair, TradeDirect direct, double diff) throws ExecutionException, InterruptedException {
        CompletableFuture<Order> future1 = CompletableFuture.supplyAsync(() -> this.createOrder(buyOrder, marketA, pair, direct, diff));
        CompletableFuture<Order> future2 = CompletableFuture.supplyAsync(() -> this.createOrder(sellOrder, marketB, pair, direct, diff));
        CompletableFuture<Void> f = CompletableFuture.allOf(future1, future2);
        f.get();
    }

    /**
     * @param orderRequest
     * @param market
     * @param pair
     * @return
     */
//    @Async FIXME：创建订单不能用异步，会导致新的交易进来不能准确计算yue
    public Order createOrder(OrderRequest orderRequest, MarketParser market, String pair, TradeDirect direct, double diffPercent) {

        // Fixme: dragonex
        if (market.getName().equals(Dragonex.PLATFORM_NAME)) {
            this.renderOrder(orderRequest, TRADE_PRICE_FLOAT);
        }

        String orderAid = this.placeOrder(orderRequest, market);

        if (StringUtils.isEmpty(orderAid)) {
            slackService.sendMessage("Order", "Create Order failed！");
            throw new TradeException("Create Order failed！");
        }
        slackService.sendMessage("Order", "Place order:" + orderRequest.toString());
        // update account;
        accountService.refreshBalancesCache(market.getName());
        // record order
        return this.orderRepository.save(Order.builder().price(orderRequest.getPrice()).amount(orderRequest.getAmount())
                .state(OrderState.none).type(orderRequest.getType()).orderPairKey(pair).diffPercent(diffPercent).tradeDirect(direct)
                .platform(market.getName())
                .orderId(orderAid).build());
    }

    /**
     * Create a single grid order
     *
     * @param orderRequest
     * @param marketParser
     * @return
     */
    public Order crateSingleOrder(OrderRequest orderRequest, MarketParser marketParser) {
        String orderAid = this.placeOrder(orderRequest, marketParser);
        if (StringUtils.isEmpty(orderAid)) {
            throw new TradeException("Create Order failed！");
        }
        // record order
        return this.orderRepository.save(Order.builder().price(orderRequest.getPrice()).amount(orderRequest.getAmount())
                .state(OrderState.none).type(orderRequest.getType()).orderPairKey("").diffPercent(0).tradeDirect(TradeDirect.NONE)
                .platform(marketParser.getName())
                .orderId(orderAid).build());
    }


    // add match Probability
    private void renderOrder(OrderRequest orderRequest, float f) {
        double price = orderRequest.getPrice();
        if (orderRequest.getType() == OrderType.BUY_LIMIT) {
            // Dragonex miss too much.
            orderRequest.setPrice(price * (1 + f));
            log.info("Dragonex render buy order from:[{}] to [{}]", price, orderRequest.getPrice());
        }
        if (orderRequest.getType() == OrderType.SELL_LIMIT) {
            orderRequest.setPrice(price * (1 - f));
            log.info("Dragonex render sell order from:[{}] to [{}]", price, orderRequest.getPrice());
        }
    }

    public Order findByOrderId(String orderId) {
        return this.orderRepository.findByOrderId(orderId);
    }


    public List<ProfitStatistics> queryProfitStatistics() {
        return this.profitStatisticsRepository.findAllByOrderByCreateTimeDesc();
    }


    @Transactional
    public Order replaceOrder(String orderId, double price) {
        Order origin = this.orderRepository.findByOrderId(orderId);
        if (orderId == null) {
            throw new ResourceException("not found order.");
        }
        // place order again.
        Order order = this.createOrder(OrderRequest.builder().amount(origin.getAmount() - origin.getFilledAmount())
                        .type(origin.getType())
                        .price(price).symbol(origin.getSymbol()).build(),
                MarketFactory.getMarket(origin.getPlatform())
                , origin.getOrderPairKey(), origin.getTradeDirect(), origin.getDiffPercent());

        if (order != null) {
            this.orderRepository.delete(origin);
        }
        return order;
    }


    public long count() {
        return this.orderRepository.count();
    }




}
