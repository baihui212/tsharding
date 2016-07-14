package com.mogujie.trade.db;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author by jiuru on 16/7/14.
 */
public class EmptyDataSourceRoutingHandler implements DataSourceRoutingHandler {

    @Override
    public String dynamicRoute(Method method, Object[] args) {
        // this will never be called.
        throw new InternalError();
    }

    @Override
    public Collection<String> values() {
        // this will never be called.
        throw new InternalError();
    }

}
