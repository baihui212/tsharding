交易分库分表组件TSharding



####
1.测试用例入口  com.mogujie.service.tsharding.test#TShardingTest

2.默认走Master库的前缀命名 com.mogujie.trade.tsharding.route.orm.base.ReadWriteSplittingContextInitializer.DEFAULT_WRITE_METHOD_NAMES

3.SQL增强 com.mogujie.trade.tsharding.route.orm.MapperResourceEnhancer.enhancedShardingSQL


####测试用例

跑测试用例之前先建库建表结构;
理论上是8个库,512张表,每个库64张表.

如果仅仅是跑测试用例,执行下面的sql就可以跑通:

create database trade0000;
create database trade0001;
create database trade0002;
create database trade0003;
create database trade0004;
create database trade0005;
create database trade0006;
create database trade0007;

create database trade;

use trade0001;
CREATE TABLE `TradeOrder0064` (
  `orderId` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `buyerUserId` bigint(20) unsigned NOT NULL COMMENT '买家的userId',
  `sellerUserId` bigint(20) unsigned NOT NULL COMMENT '卖家的userId',
  `shipTime` int(11) unsigned DEFAULT '0' COMMENT '发货时间',
  PRIMARY KEY (`orderId`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

INSERT INTO `TradeOrder0064` (`orderId`, `buyerUserId`, `sellerUserId`, `shipTime`)
VALUES
	(50000280834672, 1234567, 2345678, 12345678);
