package com.mogujie.service.tsharding.test.route;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ShopOrderMapperTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Before
    public void setUp() throws Exception {
        this.applicationContext = new ClassPathXmlApplicationContext("META-INF/support/service-loader.xml",
                "META-INF/support/datasource.xml");
        this.shopOrderMapper = applicationContext.getBean(ShopOrderMapper.class);
    }

    @After
    public void tearDown() throws Exception {
        this.applicationContext.close();
    }


    @Test
    public void testXdOrderSharding() {

        ShopOrder shopOrder = null;
        try {
            shopOrder = shopOrderMapper.getShopOrderByShopOrderId(3950627820824L);
            logger.debug("{}", shopOrder);
            Assert.assertNotNull(shopOrder);

            shopOrder = shopOrderMapper.getShopOrderByShopOrderId(3950627820002L);
            logger.debug("{}", shopOrder);
            Assert.assertNull(shopOrder);
        } catch (Exception e) {
            logger.error("test error: {}", e.getMessage(), e);
        }

    }


}
