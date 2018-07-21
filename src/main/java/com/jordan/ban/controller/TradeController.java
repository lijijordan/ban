package com.jordan.ban.controller;

import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.domain.in.Greeting;
import com.jordan.ban.entity.Account;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.TradeCounter;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.OrderService;
import com.jordan.ban.service.TradeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Controller
public class TradeController {

    @Autowired
    private TradeContext tradeContext;

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TradeCounter tradeCounter;

    @Autowired
    private TradeRecordService tradeRecordService;


    @GetMapping("/greeting")
    public String greetingForm(Model model, HttpServletRequest request) {
        // Last 24 hours
        Date date = new Date(System.currentTimeMillis() - 3600 * 24 * 1000);
        String str = request.getParameter("date");
        if (str != null) {
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        System.out.println("query date:" + date);
        model.addAttribute("greeting", Greeting.builder()
                .avgFloatPercent(this.tradeContext.getAvgFloatPercent())
                .wareHouseDiff(tradeContext.getWareHouseDiff())
                .minTradeFloat(tradeContext.getMinTradeFloat())
                .moveBackMetrics(tradeContext.getDownPoint())
                .upMax(tradeCounter.getMaxDiffPercent(true))
                .downMax(tradeCounter.getMaxDiffPercent(false))
                .moveMetrics(tradeContext.getUpPoint()).build());
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        Map<String, BalanceDto> balanceB = accountService.findBalancesCache(Dragonex.PLATFORM_NAME);

        String coinName = "eth";
        AccountDto accountA = AccountDto.builder()
                .frozen(balanceA.get(coinName).getFrozen())
                .money(balanceA.get("usdt").getBalance()).platform(Fcoin.PLATFORM_NAME).symbol("ethusdt")
                .frozenMoney(balanceA.get("usdt").getFrozen())
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getBalance() : 0).build();
        AccountDto accountB = AccountDto.builder()
                .frozen(balanceB.get(coinName).getFrozen())
                .money(balanceB.get("usdt").getBalance()).platform(Dragonex.PLATFORM_NAME).symbol("ethusdt")
                .frozenMoney(balanceB.get("usdt").getFrozen())
                .virtualCurrency(balanceB.get(coinName) != null ? balanceB.get(coinName).getBalance() : 0).build();


        String suggestText = this.tradeCounter.getSuggestDiffPercent() + "["
                + this.tradeCounter.getA2bTradeCount()
                + "]";
        model.addAttribute("orderList", this.orderService.queryOrder("ethusdt", date));
        model.addAttribute("recordList", this.tradeRecordService.queryAndStatisticTradeRecord(date));
        model.addAttribute("accountA", accountA);
        model.addAttribute("accountB", accountB);
        model.addAttribute("sumMoney", (accountA.getMoney() + accountB.getMoney()));
        model.addAttribute("sumCoin", (accountA.getVirtualCurrency() + accountB.getVirtualCurrency()));
        model.addAttribute("a2bAvgPercent", this.tradeCounter.getAvgDiffPercent(TradeDirect.A2B));
        model.addAttribute("b2aAvgPercent", this.tradeCounter.getAvgDiffPercent(TradeDirect.B2A));
        model.addAttribute("suggest", suggestText);
        model.addAttribute("profitStatistics", this.orderService.queryProfitStatistics());
        return "greeting";
    }

    @PostMapping("/greeting")
    public String greetingSubmit(@ModelAttribute Greeting greeting) {
        this.tradeContext.setUpPoint((float) greeting.getMoveMetrics());
        this.tradeContext.setDownPoint((float) greeting.getMoveBackMetrics());
        this.tradeContext.setAvgFloatPercent(greeting.getAvgFloatPercent());
        this.tradeContext.setMinTradeFloat(greeting.getMinTradeFloat());
        this.tradeContext.setWareHouseDiff(greeting.getWareHouseDiff());
        return "result";
    }


}