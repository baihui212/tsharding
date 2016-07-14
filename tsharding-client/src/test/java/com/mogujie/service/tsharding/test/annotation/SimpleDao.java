package com.mogujie.service.tsharding.test.annotation;

import com.mogujie.trade.tsharding.annotation.parameter.ShardingBuyerPara;
import org.apache.ibatis.annotations.Param;

/**
 * @auther qigong on 5/29/15 11:13 AM.
 */
public interface SimpleDao {

    public void abc(String a,String b,@ShardingBuyerPara @Param("test") Long id);

}
