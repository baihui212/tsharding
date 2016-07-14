package com.mogujie.trade.db;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author by jiuru on 16/7/14.
 */
public interface DataSourceFactory<T extends DataSource> {

    T getDataSource(DataSourceConfig config) throws SQLException;

}
