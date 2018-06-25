package com.jordan.ban.mq.spring;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Any;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Sender {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Async
    public void send(String topic, String message) {
//        log.info(message);
//        log.info("topic:{}, message:{}", topic, message);
        this.amqpTemplate.convertAndSend(topic, message);
    }
}
