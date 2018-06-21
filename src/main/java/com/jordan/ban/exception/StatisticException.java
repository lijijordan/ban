package com.jordan.ban.exception;

/**
 * ApiException if api returns error.
 *
 * @Date 2018/1/14
 * @Time 16:02
 */

public class StatisticException extends RuntimeException {

    final String errCode;

    public StatisticException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public StatisticException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public StatisticException(String errMsg) {
        super(errMsg);
        this.errCode = "error";
    }


    public String getErrCode() {
        return this.errCode;
    }

}
