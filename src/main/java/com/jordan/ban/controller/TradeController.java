package com.jordan.ban.controller;

import com.jordan.ban.domain.in.Greeting;
import com.jordan.ban.market.TradeContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TradeController {

    @Autowired
    private TradeContext tradeContext;


    @GetMapping("/greeting")
    public String greetingForm(Model model) {
        model.addAttribute("greeting", Greeting.builder()
                .moveBackMetrics(tradeContext.getMoveBackMetrics())
                .moveMetrics(tradeContext.getMoveMetrics()).build());
        return "greeting";
    }

    @PostMapping("/greeting")
    public String greetingSubmit(@ModelAttribute Greeting greeting) {
        this.tradeContext.setMoveMetrics(greeting.getMoveMetrics());
        this.tradeContext.setMoveBackMetrics(greeting.getMoveBackMetrics());
        return "result";
    }


}