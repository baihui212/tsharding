package com.mogujie.trade.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author by jiuru on 16/7/14.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceRouting {
    /**
     * 静态绑定该Mapper对应的数据源
     *
     * @return 绑定的数据源的名称
     */
    String value() default "";

    /**
     * 指定运行时路由处理的Handler，动态指定对应的数据源和数据表。<b>注意:schema不同时，事务不保证能传递下去！</b>
     *
     * @return
     */
    Class<? extends MapperRoutingHandler> handler();
}