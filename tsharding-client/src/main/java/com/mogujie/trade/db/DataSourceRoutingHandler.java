package com.mogujie.trade.db;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author by jiuru on 16/7/14.
 */
public interface DataSourceRoutingHandler {

    /**
     * 根据方法及参数动态路由
     *
     * @param method
     *            方法
     * @param args
     *            参数列表
     * @return 数据源的名称
     */
    String dynamicRoute(Method method, Object[] args);

    /**
     * 返回所有可能数据库名称。注:此方法可能在将来版本中废弃，无须做此指定。
     *
     * @return 所有可能返回的数据库名称
     */
    Collection<String> values();

}
