package com.mogujie.trade.tsharding.client;

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

    /**
     * 根据分片参数值计算分表名
     *
     * @param shardingPara
     * @return 分表名0xxx
     */
    public static String caculateTableName(Long shardingPara) {
        if (shardingPara >= 0) {
            return "XDOrder" + getNumberWithZeroSuffix((shardingPara % 10000) % 512);
        }
        return null;
    }

    /**
     * 根据分片参数值计算分表名
     *
     * @param shardingPara
     * @return 分表名0xxx
     */
    public static Integer caculateTableIndex(Long shardingPara) {
        if (shardingPara >= 0) {
            return new Long(shardingPara % 10000 % 512).intValue();
        }
        return null;
    }


    /**
     * 根据分片参数值计算分库名（逻辑库）
     *
     * @param shardingPara
     * @return 分库名000x
     */
    public static String caculateSchemaName(String fieldName, Long shardingPara) {
        if (shardingPara >= 0) {

            if ("sellerUserId".equals(fieldName)) {
                return "sellerxdtrade" + getNumberWithZeroSuffix(((shardingPara % 10000) % 512) / 64);
            } else {
                return "xdtrade" + getNumberWithZeroSuffix(((shardingPara % 10000) % 512) / 64);
            }
        }
        return null;
    }

    /**
     * 根据分片参数值计算数据源名
     *
     * @param shardingPara
     * @return DatasourceName 见数据源配置文件
     */
    public static String caculateDatasourceName(String fieldName, Long shardingPara) {
        if (shardingPara >= 0) {
            if ("sellerUserId".equals(fieldName)) {
                return "seller_ds_" + ((shardingPara % 10000) % 512) / 256;
            } else {
                return "buyer_ds_" + ((shardingPara % 10000) % 512) / 256;
            }
        }
        return null;
    }

    public static String getNumberWithZeroSuffix(long number) {
        if (number >= 100) {
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
    public static Map<Integer, List<Long>> getTableNoAndOrderIdsMap(List<Long> listShopOrderIds) {

        HashMap<Integer, List<Long>> shopOrderIdsMap = new HashMap();
        if (listShopOrderIds == null || listShopOrderIds.size() == 0) {
            return shopOrderIdsMap;
        }
        for (Long shopOrderId : listShopOrderIds) {
            Integer tableNo = ShardingCaculator.caculateTableIndex(shopOrderId);
            List<Long> orderIds = shopOrderIdsMap.get(tableNo);
            if (orderIds == null) {
                orderIds = new ArrayList<>();
            }
            orderIds.add(shopOrderId);
            shopOrderIdsMap.put(tableNo, orderIds);
        }
        return shopOrderIdsMap;
    }

    public static void main(String args[]) {
        System.out.println(caculateTableName(6000004386417L));
        System.out.println(caculateSchemaName("buyerUserId", 6000004386417L));

        System.out.println(caculateTableName(35586213L));
        System.out.println(caculateSchemaName("sellerUserId", 35586213L));
    }
}
