package com.mogujie.trade.tsharding.route.orm.base;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.db.DataSourceRoutingException;
import com.mogujie.trade.db.MapperRoutingHandler;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingBuyerPara;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingOrderPara;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingSellerPara;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class TShardingRoutingInvokeFactory implements InvokerFactory<Class<?>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private SqlSessionFactoryLookup sqlSessionFactoryLookup;

    public TShardingRoutingInvokeFactory(SqlSessionFactoryLookup sqlSessionFactoryLookup) {
        this.sqlSessionFactoryLookup = sqlSessionFactoryLookup;
    }

    @Override
    public Invoker newInvoker(Class<?> mapperInterface) {

        final DataSourceRouting dataSourceRouting = mapperInterface.getAnnotation(DataSourceRouting.class);
        final Class clazz = mapperInterface;

        if (dataSourceRouting != null && !StringUtils.isEmpty(dataSourceRouting.value())) { //使用配置的数据源
            logger.debug("TShardingRoutingInvokeFactory routing: emptyHandler and dataSourceRouting.value:" + dataSourceRouting.value());
            return new Invoker() {
                @Override
                public Object invoke(Invocation invocation) throws Throwable {

                    MapperBasicConfig config = new MapperBasicConfig(clazz, dataSourceRouting.value());
                    final Object mapper = newMyBatisMapper(config);
                    try {
                        ReadWriteSplittingContextInitializer.initReadWriteSplittingContext(invocation.getMethod());
                        return invocation.getMethod().invoke(mapper, invocation.getArgs());
                    } finally {
                        ReadWriteSplittingContextInitializer.clearReadWriteSplittingContext();
                    }
                }
            };
        } else if (dataSourceRouting != null && MapperRoutingHandler.class.isAssignableFrom(dataSourceRouting.handler())) { //使用Sharding数据源

            logger.debug("TShardingRoutingInvokeFactory routing: dynamic handler: " + dataSourceRouting.handler().getName());
            return new Invoker() {
                @Override
                public Object invoke(Invocation invocation) throws Throwable {

                    Method method = invocation.getMethod();
                    ShardingMetadata shardingMetadata = getShardingKey(method, invocation.getArgs(), dataSourceRouting);

                    if (shardingMetadata == null) {
                        throw new DataSourceRoutingException("dataSourceRouting error! Method Name:" + method.getName() + " shardingMetadata is null!");
                    }

                    //走分库分表环境
                    logger.debug("TShardingRoutingInvokeFactory routing to sharding db. Method Name:" + method.getName() + ". ShardingKey:" + shardingMetadata.getShardingKey());

                    Class newClass = clazz;
                    if (!"".equals(shardingMetadata.getSchemaName())) {
                        newClass = Class.forName(clazz.getCanonicalName() + "Sharding" + method.getName());
                    }
                    Method newMethod = newClass.getMethod(method.getName() + shardingMetadata.getTableSuffix(), method.getParameterTypes());
                    MapperBasicConfig config = new MapperBasicConfig(newClass, shardingMetadata.getSchemaName());
                    final Object mapper = newMyBatisMapper(config);
                    try {
                        ReadWriteSplittingContextInitializer.initReadWriteSplittingContext(invocation.getMethod());
                        return newMethod.invoke(mapper, invocation.getArgs());
                    } finally {
                        ReadWriteSplittingContextInitializer.clearReadWriteSplittingContext();
                    }
                }
            };
        } else {
            throw new DataSourceRoutingException("dataSourceRouting error! cannot find datasource");
        }
    }


    private ShardingMetadata getShardingKey(Method method, Object[] args, DataSourceRouting dataSourceRouting) throws NoSuchFieldException, IllegalAccessException {

        MapperRoutingHandler mapperRoutingHandler;
        try {
            mapperRoutingHandler = dataSourceRouting.handler().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Annotation[][] an = method.getParameterAnnotations();
        if (an.length > 0) {
            for (int i = 0; i < an.length; i++) {
                for (int j = 0; j < an[i].length; j++) {
                    if (an[i][j] instanceof ShardingOrderPara || an[i][j] instanceof ShardingBuyerPara || an[i][j] instanceof ShardingSellerPara) {
                        Long shardingKey = 0L;
                        if (args[i] instanceof Long) {
                            shardingKey = (Long) args[i];
                        } else if (args[i] instanceof List) {
                            shardingKey = (Long) ((List) args[i]).get(0);
                        } else if (an[i][j] instanceof ShardingOrderPara && args[i] instanceof Object) {
                            Field field = ReflectUtil.getDeclaredField(args[i], "orderId");
                            field.setAccessible(true);
                            shardingKey = (Long) field.get(args[i]);
                            if (shardingKey == null) {
                                field = ReflectUtil.getDeclaredField(args[i], "parentOrderId");
                                field.setAccessible(true);
                                shardingKey = (Long) field.get(args[i]);
                            }
                        } else if (an[i][j] instanceof ShardingBuyerPara && args[i] instanceof Object) {
                            Field field = ReflectUtil.getDeclaredField(args[i], "buyerUserId");
                            field.setAccessible(true);
                            shardingKey = (Long) field.get(args[i]);
                        } else if (an[i][j] instanceof ShardingSellerPara && args[i] instanceof Object) {
                            Field field = ReflectUtil.getDeclaredField(args[i], "sellerUserId");
                            field.setAccessible(true);
                            shardingKey = (Long) field.get(args[i]);
                        }

                        ShardingMetadata shardingMetadata = new ShardingMetadata();
                        shardingMetadata.setShardingKey(shardingKey);
                        shardingMetadata.setTableSuffix(mapperRoutingHandler.calculateTableIndex(shardingKey));
                        shardingMetadata.setSchemaName(mapperRoutingHandler.calculateSchemaName(shardingKey));
                        return shardingMetadata;
                    }
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Object newMyBatisMapper(MapperBasicConfig config) {
        MapperFactoryBean mapperFactoryBean = new MapperFactoryBean();
        mapperFactoryBean.setMapperInterface(config.getMapperInterface());
        mapperFactoryBean.setSqlSessionFactory(this.getSqlSessionFactory(config.getDataSourceName(),
                config.getMapperInterface()));
        mapperFactoryBean.afterPropertiesSet();
        Object mapper = null;
        try {
            mapper = mapperFactoryBean.getObject();
        } catch (Exception e) {
            throw new MapperInitializeException(e);
        }
        return mapper;
    }

    private SqlSessionFactory getSqlSessionFactory(String dataSourceName, Class<?> mapperInterface) {
        if (StringUtils.isEmpty(dataSourceName)) {
            if (sqlSessionFactoryLookup.getMapping().size() == 1) {
                return sqlSessionFactoryLookup.getMapping().values().iterator().next();
            } else {
                throw new DataSourceRoutingException("can't decided the datasource of "
                        + mapperInterface.getCanonicalName() + ",please add config by using @DataSourceRouting");
            }
        } else {
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryLookup.get(dataSourceName);
            if (sqlSessionFactory == null) {
                throw new DataSourceRoutingException("can't find datasource named " + dataSourceName
                        + " while init!");
            }
            return sqlSessionFactory;
        }
    }

    private class ShardingMetadata {

        private Long shardingKey;

        private String schemaName;

        private String tableSuffix;

        public String getSchemaName() {
            return schemaName;
        }

        public void setSchemaName(String schemaName) {
            this.schemaName = schemaName;
        }

        public String getTableSuffix() {
            return tableSuffix;
        }

        public void setTableSuffix(String tableSuffix) {
            this.tableSuffix = tableSuffix;
        }

        public Long getShardingKey() {
            return shardingKey;
        }

        public void setShardingKey(Long shardingKey) {
            this.shardingKey = shardingKey;
        }
    }
}
