package com.mogujie.service.tsharding.test;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.dao.ShopOrderDao;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by jiuru on 16/7/14.
 */
public class TShardingTest extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(TShardingTest.class);

    @Autowired
    private ShopOrderDao shopOrderDao;


    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Test
    public void testGetShopOrderByShopOrderIdsOfOneBuyer() {
        List<Long> orderIds = new ArrayList<>();
        orderIds.add(111L);
        List<ShopOrder> orders = shopOrderDao.getShopOrderByShopOrderIdsOfOneBuyer(orderIds);
        System.out.println();
    }


    @Test
    public void testGetShopOrderByShopOrderIds() {
        List<Long> orderIds = new ArrayList<>();
        orderIds.add(50000280834672L);
//        orderIds.add(70000204797232L);
        List<ShopOrder> orders = shopOrderMapper.getShopOrderByShopOrderIds(orderIds);
        System.out.println();
    }


}
