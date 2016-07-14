package com.mogujie.trade.db;

import javax.sql.DataSource;

/**
 * @author by jiuru on 16/7/14.
 */
public class RoutingDataSourceTransactionContext {

    private static final ThreadLocal<DataSource> curDataSource = new ThreadLocal<DataSource>();

    public static DataSource getCurTransactionDataSource() {
        return curDataSource.get();
    }

    public static void setCurDataSource(DataSource dataSource) {
        curDataSource.set(dataSource);
    }

    public static void clear() {
        curDataSource.remove();
    }
}