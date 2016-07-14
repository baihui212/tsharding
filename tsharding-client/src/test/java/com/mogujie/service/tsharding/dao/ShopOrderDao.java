package com.mogujie.service.tsharding.dao;

import com.mogujie.service.tsharding.bean.ShopOrder;

import java.util.List;

/**
 * @auther qigong on 6/5/15 8:50 PM.
 */
public interface ShopOrderDao {

    /**
     * 根据店铺级订单ID获取订单信息（同一个买家）
     *
     * @param listShopOrderIds 店铺级订单ID集合
     * @return List<XdShopOrder>
     */
    List<ShopOrder> getShopOrderByShopOrderIdsOfOneBuyer(List<Long> listShopOrderIds);

    /**
     * 根据店铺级订单ID获取订单信息（多个买家）
     *
     * @param listShopOrderIds 店铺级订单ID集合
     * @return List<XdShopOrder>
     */
    List<ShopOrder> getShopOrderByShopOrderIdsOfMultiBuyer(List<Long> listShopOrderIds);


}
