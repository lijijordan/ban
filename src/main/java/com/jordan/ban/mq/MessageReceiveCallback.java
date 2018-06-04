package com.jordan.ban.mq;

import org.json.JSONException;

public interface MessageReceiveCallback {
    void callback(String topic, String message) throws JSONException;
}
