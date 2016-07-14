package com.mogujie.trade.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author by jiuru on 16/7/14.
 */
public class RoutingDataSourceTransactionManager extends DataSourceTransactionManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;
    /**
     *
     */
    private static final long serialVersionUID = 2532966497797909623L;

    public RoutingDataSourceTransactionManager() {
    }

    public RoutingDataSourceTransactionManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected Object doGetTransaction() {
        RoutingDataSourceTransactionContext.setCurDataSource(getDataSource());
        logger.debug("Transaction of {} begin", this.name);
        return super.doGetTransaction();
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        RoutingDataSourceTransactionContext.clear();
        logger.debug("Transaction of {} ends", this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
