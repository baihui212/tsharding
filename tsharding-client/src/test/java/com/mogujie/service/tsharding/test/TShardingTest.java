package com.mogujie.service.tsharding.test;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.dao.ShopOrderDao;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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
    public void testGetShopOrderByShopOrderIdsDao() {
        List<Long> orderIds = new ArrayList<>();
        orderIds.add(50000280834672L);
        List<ShopOrder> orders = shopOrderDao.getShopOrderByShopOrderIds(orderIds);
        Assert.isTrue(orders.get(0).getOrderId().equals(50000280834672L));
    }

    @Test
    public void testGetShopOrderByShopOrderIds() {
        List<Long> orderIds = new ArrayList<>();
        orderIds.add(50000280834672L);
        List<ShopOrder> orders = shopOrderMapper.getShopOrderByShopOrderIds(orderIds);
        Assert.isTrue(orders.get(0).getOrderId().equals(50000280834672L));
    }

    @Test
    public void testUpdateShopOrder() {
        List<Long> orderIds = new ArrayList<>();
        orderIds.add(50000280834672L);
        ShopOrder shopOrder = new ShopOrder();
        shopOrder.setShipTime(12345678L);
        int rows = shopOrderMapper.batchUpdateShopOrderByShopOrderIds(orderIds, shopOrder);
        Assert.isTrue(rows == 1);
    }


}
