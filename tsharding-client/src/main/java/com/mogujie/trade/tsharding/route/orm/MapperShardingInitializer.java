package com.mogujie.trade.tsharding.route.orm;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 增强Mapper处理总入口：Mapper被mybatis初始化后，在这里做进一步的处理和增强
 *
 * @author qigong on 5/1/15
 */
public class MapperShardingInitializer implements ApplicationContextAware {


    Logger logger = LoggerFactory.getLogger(getClass());

    private String needEnhancedClasses;
    private String[] needEnhancedClassesArray;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, SqlSessionFactory> sqlSessionFactories = applicationContext.getBeansOfType(SqlSessionFactory.class);
        if (sqlSessionFactories.isEmpty()) {
            return;
        }
        MapperHelperForSharding mapperHelperForSharding = new MapperHelperForSharding();
        List<SqlSession> sqlSessions = new ArrayList<>(sqlSessionFactories.size());
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories.values()) {
            SqlSession sqlSession = new SqlSessionTemplate(sqlSessionFactory);
            sqlSessions.add(sqlSession);
        }
        //Mapper代码增强 每个方法扩展出一个ShardingMapper类，增强为512个方法。
        this.needEnhancedClassesArray = needEnhancedClasses.split(",");
        this.enhanceMapperClass();
        mapperHelperForSharding.setMappers(needEnhancedClassesArray);
        mapperHelperForSharding.setSqlSessions(sqlSessions.toArray(new SqlSession[0]));
        mapperHelperForSharding.initMapper();
    }

    private void enhanceMapperClass() {
        for (String mapperClass : needEnhancedClassesArray) {
            try {
                MapperEnhancer.enhanceMapperClass(mapperClass);
            } catch (Exception e) {
                logger.error("Enhance {} class error", mapperClass, e);
            }
        }
    }

    public void setNeedEnhancedClasses(String needEnhancedClasses) {
        this.needEnhancedClasses = needEnhancedClasses;
    }
}
