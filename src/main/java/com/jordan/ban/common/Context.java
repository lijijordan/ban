package com.jordan.ban.common;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Context {

    // 未完成的订单数量
    private static AtomicInteger unFilledOrderNum = new AtomicInteger();

    public static int getUnFilledOrderNum() {
        return unFilledOrderNum.get();
    }

    public static void setUnFilledOrderNum(int val) {
        unFilledOrderNum.set(val);
    }
}
