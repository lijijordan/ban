package com.jordan.ban.exception;

/**
 * ApiException if api returns error.
 *
 * @Date 2018/1/14
 * @Time 16:02
 */

public class ResourceException extends RuntimeException {

    final String errCode;

    public ResourceException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public ResourceException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public ResourceException(String errMsg) {
        super(errMsg);
        this.errCode = "error";
    }


    public String getErrCode() {
        return this.errCode;
    }

}
