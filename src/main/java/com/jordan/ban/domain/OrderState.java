package com.jordan.ban.domain;

/**
 * submitted	已提交
 * * partial_filled	部分成交
 * * partial_canceled	部分成交已撤销
 * * filled	完全成交
 * * canceled	已撤销
 * * pending_cancel	撤销已提交
 * <p>
 * none
 */
public enum OrderState {
    submitted, partial_filled, partial_canceled, filled, canceled, pending_cancel, created, none
}
