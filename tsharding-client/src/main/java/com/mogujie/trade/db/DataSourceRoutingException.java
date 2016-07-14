package com.mogujie.trade.db;

/**
 * @author by jiuru on 16/7/14.
 */
public class DataSourceRoutingException extends RuntimeException {

    private static final long serialVersionUID = 8678631953659495594L;

    public DataSourceRoutingException() {
    }

    public DataSourceRoutingException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public DataSourceRoutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceRoutingException(String message) {
        super(message);
    }

    public DataSourceRoutingException(Throwable cause) {
        super(cause);
    }

}
