package com.mogujie.service.tsharding.test.annotation;

import com.mogujie.trade.tsharding.annotation.parameter.ShardingBuyerPara;
import org.apache.ibatis.annotations.Param;

/**
 * @auther qigong on 5/29/15 2:24 PM.
 */
public class SimpleDaoImpl implements SimpleDao {


    public void abc(String a,String b,@ShardingBuyerPara @Param("xxx") Long id){

        System.out.println("real method");
    }
}
