package com.mogujie.service.tsharding.test.route;

import com.mogujie.trade.tsharding.annotation.parameter.ShardingOrderPara;
import com.mogujie.trade.tsharding.route.orm.base.Invocation;

/**
 * @author qigong
 *
 */
public interface InvokerTest {

    Object invoke(@ShardingOrderPara("test") Invocation invocation) throws Throwable;
}
