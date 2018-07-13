package com.jordan.ban.service;

import com.jordan.ban.dao.OrderRepository;
import com.jordan.ban.dao.ProfitStatisticsRepository;
import com.jordan.ban.dao.TradeStatisticsRepository;
import com.jordan.ban.domain.*;
import com.jordan.ban.entity.Order;
import com.jordan.ban.entity.ProfitStatistics;
import com.jordan.ban.entity.TradeStatistics;
import com.jordan.ban.exception.StatisticException;
import com.jordan.ban.exception.TradeException;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.parser.MarketFactory;
import com.jordan.ban.market.parser.MarketParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
        log.info("refreshOrderState");
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
                log.info("wait for 5s! update balance.");
                try {
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.accountService.queryAndUpdateBalancesCache(order.getPlatform());
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
//                this.accountService.queryAndUpdateBalancesCache(order.getPlatform());
                this.statisticTrade(order);
            }
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
            log.info("Order :{} waiting for trade, can not statistic.", order);
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


    @Async
    public void cancelOrder(Order order) {
        MarketParser marketParser = MarketFactory.getMarket(order.getPlatform());
        if (marketParser.cancelOrder(order.getOrderId())) {
            order.setState(OrderState.canceled);
            order.setUpdateTime(new Date());
            this.orderRepository.save(order);
        }
    }


    /**
     * 创建订单
     *
     * @param orderRequest
     * @param market
     * @param pair
     * @return
     */
//    @Async FIXME：创建订单不能用异步，会导致新的交易进来不能准确计算yue
    public Order createOrder(OrderRequest orderRequest, MarketParser market, String pair, TradeDirect direct, double diffPercent) {
        // debug
        /*log.info("market:{},request:{}", market.getName(), orderRequest.toString());
        return null;*/
        String orderAid = market.placeOrder(orderRequest);
        if (StringUtils.isEmpty(orderAid)) {
            slackService.sendMessage("Order", "Create Order failed！");
            throw new TradeException("Create Order failed！");
        }
        slackService.sendMessage("Order", "Place order:" + orderRequest.toString());
        // update account;
        accountService.queryAndUpdateBalancesCache(market.getName());
        // record order
        return this.orderRepository.save(Order.builder().price(orderRequest.getPrice()).amount(orderRequest.getAmount())
                .state(OrderState.none).type(orderRequest.getType()).orderPairKey(pair).diffPercent(diffPercent).tradeDirect(direct)
                .platform(market.getName())
                .orderId(orderAid).build());
    }

    public Order findByOrderId(String orderId) {
        return this.orderRepository.findByOrderId(orderId);
    }


    public List<ProfitStatistics> queryProfitStatistics() {
        return this.profitStatisticsRepository.findAllByOrderByCreateTimeDesc();
    }
}
