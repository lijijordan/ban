package com.jordan.ban.controller;

import com.jordan.ban.domain.AccountDto;
import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.TradeDirect;
import com.jordan.ban.domain.in.Greeting;
import com.jordan.ban.market.TradeContext;
import com.jordan.ban.market.TradeCounter;
import com.jordan.ban.market.parser.Dragonex;
import com.jordan.ban.market.parser.Fcoin;
import com.jordan.ban.service.AccountService;
import com.jordan.ban.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

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


    @GetMapping("/greeting")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", Greeting.builder()
                .moveBackMetrics(tradeContext.getDownPoint())
                .moveMetrics(tradeContext.getUpPoint()).build());
        Map<String, BalanceDto> balanceA = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        Map<String, BalanceDto> balanceB = accountService.findBalancesCache(Dragonex.PLATFORM_NAME);

        String coinName = "eth";
        AccountDto accountA = AccountDto.builder()
                .frozen(balanceA.get(coinName).getFrozen())
                .money(balanceA.get("usdt").getAvailable()).platform(Fcoin.PLATFORM_NAME).symbol("ethusdt")
                .virtualCurrency(balanceA.get(coinName) != null ? balanceA.get(coinName).getAvailable() : 0).build();
        AccountDto accountB = AccountDto.builder()
                .frozen(balanceB.get(coinName).getFrozen())
                .money(balanceB.get("usdt").getAvailable()).platform(Dragonex.PLATFORM_NAME).symbol("ethusdt")
                .virtualCurrency(balanceB.get(coinName) != null ? balanceB.get(coinName).getAvailable() : 0).build();

        String suggestText = this.tradeCounter.getSuggestDiffPercent() + "["
                + this.tradeCounter.getA2bTradeCount()
                + "]";

        model.addAttribute("orderList", this.orderService.queryOrder("ethusdt"));
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
        return "result";
    }


}