package com.mogujie.service.tsharding.mapper;

import com.mogujie.service.tsharding.bean.ShopOrder;
import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.tsharding.annotation.ShardingExtensionMethod;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingOrderPara;
import com.mogujie.trade.tsharding.route.TShardingRoutingHandler;
import com.mogujie.trade.tsharding.route.orm.MapperResourceEnhancer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DataSourceRouting(handler = TShardingRoutingHandler.class)
public interface ShopOrderMapper {

    @ShardingExtensionMethod(type = MapperResourceEnhancer.class, method = "enhancedShardingSQL")
    public ShopOrder getShopOrderByShopOrderId(@ShardingOrderPara Long shopOrderId);

    @ShardingExtensionMethod(type = MapperResourceEnhancer.class, method = "enhancedShardingSQL")
    public List<ShopOrder> getShopOrderByShopOrderIds(@ShardingOrderPara List<Long> shopOrderIds);


    @ShardingExtensionMethod(type = MapperResourceEnhancer.class, method = "enhancedShardingSQL")
    int batchUpdateShopOrderByShopOrderIds(@ShardingOrderPara @Param("shopOrderIds") List<Long> shopOrderIds, @Param("shopOrder") ShopOrder shopOrder);

}