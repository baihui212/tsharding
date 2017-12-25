package com.mogujie.service.tsharding.test.client;

import com.mogujie.trade.tsharding.client.ShardingCaculator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @auther qigong on 5/29/15 8:28 AM.
 */
public class ShardingCaculatorTest {


    @Test
    public void testCaculateTableName() {

        ShardingparaObj para = new ShardingparaObj();
        para.setName("buyerId");
        para.setValue(100000000L);
        //TODO 变成新的计算器的case 王相
//        Assert.assertEquals("TestTable0000", ShardingCaculator.caculateTableName(para.getValue()));
//        para.setValue(100000128L);
//        Assert.assertEquals("TestTable0128", ShardingCaculator.caculateTableName(para.getValue()));
//        para.setValue(100000512L);
//        Assert.assertEquals("TestTable0000", ShardingCaculator.caculateTableName(para.getValue()));
    }

    @Test
    public void testCaculateSchemaName() {

        ShardingparaObj para = new ShardingparaObj();
        para.setName("sellerUserId");
        para.setValue(100000000L);
//        Assert.assertEquals("sellertestschema0000", ShardingCaculator.caculateSchemaName(para.getName(), para.getValue()));
//        para.setValue(100000128L);
//        Assert.assertEquals("sellertestschema0002", ShardingCaculator.caculateSchemaName(para.getName(), para.getValue()));
//        para.setName("buyerUserId");
//        para.setValue(100000512L);
//        Assert.assertEquals("testschema0000", ShardingCaculator.caculateSchemaName(para.getName(), para.getValue()));
    }

    @Test
    public void testCaculateDatasourceName() {

        ShardingparaObj para = new ShardingparaObj();
        para.setName("sellerUserId");
        para.setValue(100000000L);
//        Assert.assertEquals("seller_ds_0", ShardingCaculator.caculateDatasourceName(para.getName(), para.getValue()));
//        para.setValue(100000128L);
//        Assert.assertEquals("seller_ds_0", ShardingCaculator.caculateDatasourceName(para.getName(), para.getValue()));
//        para.setName("buyerUserId");
//        para.setValue(100000511L);
//        Assert.assertEquals("buyer_ds_1", ShardingCaculator.caculateDatasourceName(para.getName(), para.getValue()));
    }

    @Test
    public void testgetNumberWithZeroSuffix(){
        Assert.assertEquals("0100", ShardingCaculator.getNumberWithZeroSuffix(100L));
    }

    private class ShardingparaObj {
        private String name;
        private Long value;

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Long getValue() {
            return value;
        }

    }
}
