package com.mogujie.service.tsharding.dao;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.service.tsharding.mapper.ShopOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @auther qigong on 6/5/15 8:52 PM.
 */
@Service("shopOrderDao")
public class ShopOrderDaoImpl implements ShopOrderDao {

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    @Override
    public List<ShopOrder> getShopOrderByShopOrderIds(List<Long> listShopOrderIds) {
        if (listShopOrderIds == null || listShopOrderIds.size() == 0) {
            return null;
        }
        Set<Long> setShopOrderIds = new HashSet<Long>();
        for (Long iShopOrderId : listShopOrderIds) {
            if (iShopOrderId > 0) {
                setShopOrderIds.add(iShopOrderId);
            }
        }
        return shopOrderMapper.getShopOrderByShopOrderIds(new ArrayList<Long>(setShopOrderIds));
    }

}
