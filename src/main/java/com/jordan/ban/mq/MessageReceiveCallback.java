package com.jordan.ban.mq;

public interface MessageReceiveCallback {
    void callback(String topic, String message);
}
