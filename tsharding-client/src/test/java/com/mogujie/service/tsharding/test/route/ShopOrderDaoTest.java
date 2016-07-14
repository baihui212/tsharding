package com.mogujie.service.tsharding.test.route;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.dao.ShopOrderDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @auther qigong on 6/5/15 8:55 PM.
 */
public class ShopOrderDaoTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurableApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        this.applicationContext = new ClassPathXmlApplicationContext("META-INF/support/service-loader.xml",
                "META-INF/support/datasource.xml");
        this.shopOrderDao = applicationContext.getBean(ShopOrderDao.class);
    }


    private ShopOrderDao shopOrderDao;

    @Test
    public void testGetShopOrderByShopOrderIdsOfOneBuyer() {

        List<Long> listShopOrderIds = new ArrayList<>();
        listShopOrderIds.add(3968484880824L);
        listShopOrderIds.add(3968484880824L);

        List<ShopOrder> shopOrders = shopOrderDao.getShopOrderByShopOrderIdsOfOneBuyer(listShopOrderIds);
        Assert.assertTrue(shopOrders.size() == 2);
    }
}
