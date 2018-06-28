package com.jordan.ban.controller;

import com.jordan.ban.domain.BalanceDto;
import com.jordan.ban.domain.in.Greeting;
import com.jordan.ban.market.TradeContext;
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

    @GetMapping("/greeting")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", Greeting.builder()
                .moveBackMetrics(tradeContext.getMoveBackMetrics())
                .moveMetrics(tradeContext.getMoveMetrics()).build());
        Map<String, BalanceDto> fcoinBalance = accountService.findBalancesCache(Fcoin.PLATFORM_NAME);
        Map<String, BalanceDto> dragonexBalance = accountService.findBalancesCache(Dragonex.PLATFORM_NAME);

        model.addAttribute("orderList", this.orderService.queryOrder("ethusdt"));
        model.addAttribute("fcoinBalance", fcoinBalance.toString());
        model.addAttribute("dragonexBalance", dragonexBalance.toString());
        return "greeting";
    }

    @PostMapping("/greeting")
    public String greetingSubmit(@ModelAttribute Greeting greeting) {
        this.tradeContext.setMoveMetrics(greeting.getMoveMetrics());
        this.tradeContext.setMoveBackMetrics(greeting.getMoveBackMetrics());
        return "result";
    }


}