package com.mogujie.trade.tsharding.route.orm.base;

import com.mogujie.trade.db.DataSourceType;
import com.mogujie.trade.db.ReadWriteSplitting;
import com.mogujie.trade.db.ReadWriteSplittingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 读写分离的上下文初始化和清空
 *
 * @author qigong
 */
public class ReadWriteSplittingContextInitializer {

    private final static Logger logger = LoggerFactory.getLogger(ReadWriteSplittingContextInitializer.class);

    private final static  String[] DEFAULT_WRITE_METHOD_NAMES = {"update", "save", "insert", "delete", "add",
            "batchInsert", "batchUpdate", "batchSave", "batchAdd"};

    private final static Map<Method, DataSourceType> cache = new ConcurrentHashMap<>();

    public static void initReadWriteSplittingContext(Method method) {
        // 忽略object的方法，只关注Mapper的方法
        if (method.getDeclaringClass() != Object.class) {
        }
        DataSourceType dataSourceType = getDataSourceType(method);
        logger.debug("ReadWriteSplitting {} using dataSource of  {}", method, dataSourceType);

        ReadWriteSplittingContext.set(dataSourceType);
    }

    public static void clearReadWriteSplittingContext() {
        ReadWriteSplittingContext.clear();
    }

    /**
     * 获取方法对应的数据眼类型
     *
     * @param method
     * @return
     */
    private static DataSourceType getDataSourceType(Method method) {
        DataSourceType dataSourceType = cache.get(method);
        if (dataSourceType == null) {
            synchronized (method) {
                dataSourceType = cache.get(method);
                if (dataSourceType == null) {
                    dataSourceType = determineDataSourceType(method);
                    cache.put(method, dataSourceType);
                }
            }
        }
        return dataSourceType;
    }

    private static DataSourceType determineDataSourceType(Method method) {
        DataSourceType dataSourceType = DataSourceType.slave;

        ReadWriteSplitting readWriteSplitting = method.getAnnotation(ReadWriteSplitting.class);
        if (readWriteSplitting != null) {
            dataSourceType = readWriteSplitting.value();
            dataSourceType = dataSourceType == null ? DataSourceType.master : dataSourceType;
        } else {
            for (String writeMethodName : DEFAULT_WRITE_METHOD_NAMES) {
                if (method.getName().startsWith(writeMethodName)) {
                    dataSourceType = DataSourceType.master;
                    break;
                }
            }
        }
        return dataSourceType;
    }

}
