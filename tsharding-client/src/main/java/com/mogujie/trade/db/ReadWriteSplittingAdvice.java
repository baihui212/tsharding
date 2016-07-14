package com.mogujie.trade.db;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by jiuru on 16/7/14.
 */
public class ReadWriteSplittingAdvice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Method, DataSourceType> cache = new ConcurrentHashMap<>();

    @Around("@annotation(com.mogujie.trade.db.ReadWriteSplitting)")
    public Object intercept(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Signature signature = proceedingJoinPoint.getSignature();
        DataSourceType dataSourceType = null;
        if (signature instanceof MethodSignature) {
            // 若已经在事务中 则不做处理
            if (RoutingDataSourceTransactionContext.getCurTransactionDataSource() != null) {
                return proceedingJoinPoint.proceed();
            }

            // 若已经设置为Master 则忽略
            if (ReadWriteSplittingContext.isMaster()) {
                return proceedingJoinPoint.proceed();
            }
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            dataSourceType = this.getDataSourceType(method);
        } else {
            // this may not happend.
            throw new ReadWriteSplittingException("ReadWriteSplitting annotation should only used on method. ");
        }
        ReadWriteSplittingContext.set(dataSourceType);
        logger.debug("{} {} using dataSourceOf {} ", proceedingJoinPoint.getTarget(),
                proceedingJoinPoint.getSignature(), dataSourceType);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            ReadWriteSplittingContext.clear();
            logger.debug("{} release dataSource of {}", proceedingJoinPoint.getTarget(), dataSourceType);
        }
    }

    /**
     * 获取方法对应的数据源类型
     *
     * @param method
     * @return
     */
    private DataSourceType getDataSourceType(Method method) {
        DataSourceType dataSourceType = this.cache.get(method);
        if (dataSourceType == null) {
            synchronized (method) {
                dataSourceType = this.cache.get(method);
                if (dataSourceType == null) {
                    dataSourceType = this.determineDataSourceType(method);
                    this.cache.put(method, dataSourceType);
                }
            }
        }
        return dataSourceType;
    }

    private DataSourceType determineDataSourceType(Method method) {
        DataSourceType dataSourceType = DataSourceType.slave;

        ReadWriteSplitting readWriteSplitting = method.getAnnotation(ReadWriteSplitting.class);
        if (readWriteSplitting != null) {
            dataSourceType = readWriteSplitting.value();
            dataSourceType = dataSourceType == null ? DataSourceType.master : dataSourceType;
        } else {
            // this will not happen
            throw new InternalError("method must have the annotation of ReadWriteSplitting! ");
        }

        Transactional transcational = method.getAnnotation(Transactional.class);
        if (transcational != null) {
            throw new ReadWriteSplittingException("ReadWriteSplitting and  Transactional can't be used on method "
                    + method + " at the same time!");
        }
        return dataSourceType;
    }

}
