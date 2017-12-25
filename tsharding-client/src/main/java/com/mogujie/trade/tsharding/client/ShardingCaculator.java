package com.mogujie.trade.tsharding.client;

import com.mogujie.trade.db.MapperRoutingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分片计算器
 *
 * @auther qigong on 5/28/15 1:06 PM.
 */
public class ShardingCaculator {

    public static String getNumberWithZeroSuffix(long number) {
        if (number >= 1000) {
            return "" + number;
        } else if (number >= 100) {
            return "0" + number;
        } else if (number >= 10) {
            return "00" + number;
        } else if (number >= 0) {
            return "000" + number;
        }
        return null;
    }

    /**
     * 按订单号批量查询：跨表查，先按分表做分组
     *
     * @param listShopOrderIds
     * @return tableNo -> orderIds
     */
    public static Map<String, List<Long>> getTableNoAndOrderIdsMap(List<Long> listShopOrderIds, MapperRoutingHandler routingHandler) {

        HashMap<String, List<Long>> shopOrderIdsMap = new HashMap();
        if (listShopOrderIds == null || listShopOrderIds.size() == 0) {
            return shopOrderIdsMap;
        }
        for (Long shopOrderId : listShopOrderIds) {
            String tableNo = routingHandler.calculateTableIndex(shopOrderId);
            List<Long> orderIds = shopOrderIdsMap.get(tableNo);
            if (orderIds == null) {
                orderIds = new ArrayList<>();
            }
            orderIds.add(shopOrderId);
            shopOrderIdsMap.put(tableNo, orderIds);
        }
        return shopOrderIdsMap;
    }
}
