package com.jordan.ban.exception;

/**
 * ApiException if api returns error.
 *
 * @Date 2018/1/14
 * @Time 16:02
 */

public class TradeException extends RuntimeException {

    final String errCode;

    public TradeException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public TradeException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public TradeException(String errMsg) {
        super(errMsg);
        this.errCode = "error";
    }


    public String getErrCode() {
        return this.errCode;
    }

}
