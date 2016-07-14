package com.mogujie.trade.db;

/**
 * @author by jiuru on 16/7/14.
 */
public class ReadWriteSplittingContext {

    private static final ThreadLocal<DataSourceType> curDataSourceType = new ThreadLocal<DataSourceType>();

    public static void set(DataSourceType dataSourceType) {
        curDataSourceType.set(dataSourceType);
    }

    public static void setMaster() {
        curDataSourceType.set(DataSourceType.master);
    }

    public static void clear() {
        curDataSourceType.remove();
    }

    public static boolean isMaster() {
        return DataSourceType.master == curDataSourceType.get();
    }

}
