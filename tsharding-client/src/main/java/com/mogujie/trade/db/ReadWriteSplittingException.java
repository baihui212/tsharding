package com.mogujie.trade.db;

/**
 * @author by jiuru on 16/7/14.
 */
public class ReadWriteSplittingException extends RuntimeException {

    private static final long serialVersionUID = 3118672534738018794L;

    public ReadWriteSplittingException() {
    }

    public ReadWriteSplittingException(String message) {
        super(message);
    }

    public ReadWriteSplittingException(Throwable cause) {
        super(cause);
    }

    public ReadWriteSplittingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadWriteSplittingException(String message, Throwable cause, boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
