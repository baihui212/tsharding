package com.mogujie.trade.db;

/**
 * Created by bob on 17/1/10.
 */
public interface MapperRoutingHandler {

    /**
     * 分成多少张表
     *
     * @return
     */
    int tableSize();

    String calculateTableIndex(Long shardingPara);

    /**
     * 数据库名
     *
     * @param shardingPara
     * @return
     */
    String calculateSchemaName(Long shardingPara);

    /**
     * 表名前缀
     *
     * @return
     */
    String baseTableName();

    /**
     * 运行时获得新表名
     *
     * @return
     */
    String newTableName(Long shardingPara);

    /**
     * 计算出原方法名字
     *
     * @param method
     * @return
     */
    String methodOriginName(String method);
}