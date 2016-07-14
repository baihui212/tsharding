package com.mogujie.trade.tsharding.route;


import com.mogujie.trade.db.DataSourceRoutingHandler;

import java.lang.reflect.Method;
import java.util.Collection;


/**
 * @author qigong 06/05/2015
 */
public class TShardingRoutingHandler implements DataSourceRoutingHandler {

    @Override
    public String dynamicRoute(Method method, Object[] args) {
        //route逻辑见TShardingRoutingInvokeFactory
        return "testschema";
    }

    @Override
    public Collection<String> values() {
        //暂未使用
        return null;
    }
}
