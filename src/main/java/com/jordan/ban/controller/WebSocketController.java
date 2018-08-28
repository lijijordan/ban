package com.jordan.ban.controller;

import com.jordan.ban.domain.Message;
import com.jordan.ban.domain.Response;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


@Controller
public class WebSocketController {

    @MessageMapping("/welcome")
    @SendTo("/topic/getResponse")
    public Response welcome(Message message) {
        return new Response("Message: " + message.getMessage());
    }
}