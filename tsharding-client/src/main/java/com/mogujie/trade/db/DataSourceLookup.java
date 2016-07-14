package com.mogujie.trade.db;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author by jiuru on 16/7/14.
 */
public class DataSourceLookup implements Closeable {

    private final Map<String, ReadWriteSplittingDataSource> dataSources;

    public DataSourceLookup(Map<String, ReadWriteSplittingDataSource> dataSources) {
        this.dataSources = Collections.unmodifiableMap(dataSources);
    }

    /**
     * @param name
     * @return
     */
    public ReadWriteSplittingDataSource get(String name) {
        return this.dataSources.get(name);
    }

    public Map<String, ReadWriteSplittingDataSource> getMapping() {
        return this.dataSources;
    }

    @Override
    public void close() throws IOException {
        for (ReadWriteSplittingDataSource dataSource : dataSources.values()) {
            dataSource.close();
        }
    }
}