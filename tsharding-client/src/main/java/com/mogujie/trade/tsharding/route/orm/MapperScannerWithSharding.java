package com.mogujie.trade.tsharding.route.orm;

import com.mogujie.trade.db.DataSourceLookup;
import com.mogujie.trade.db.ReadWriteSplittingDataSource;
import com.mogujie.trade.tsharding.route.orm.base.*;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tsharding MybatisMapper的扫描类，负责将Mapper接口与对应的xml配置文件整合，绑定设定的数据源，注入到Spring Context中。
 *
 * @author qigong
 */
public class MapperScannerWithSharding implements BeanFactoryPostProcessor, InitializingBean {

    public static DataSourceLookup dataSourceLookup;

    private String packageName;

    private Resource[] mapperLocations;

    private String[] mapperPacakages;

    private SqlSessionFactoryLookup sqlSessionFactoryLookup;

    public static DataSourceLookup getDataSourceLookup() {
        return dataSourceLookup;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initMapperPackage();
    }

    private void initMapperPackage() throws IOException {
        this.mapperPacakages = packageName.split(",");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.dataSourceLookup = beanFactory.getBean(DataSourceLookup.class);

        try {
            this.initSqlSessionFactories(beanFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ClassPathScanHandler scanner = new ClassPathScanHandler();
        Set<Class<?>> mapperClasses = new HashSet<>();
        for (String mapperPackage : this.mapperPacakages) {
            Set<Class<?>> classes = scanner.getPackageAllClasses(mapperPackage.trim(), false);
            mapperClasses.addAll(classes);
        }
        for (Class<?> clazz : mapperClasses) {
            if (isMapper(clazz)) {
                Object mapper = this.newMapper(clazz);
                beanFactory.registerSingleton(Character.toLowerCase(clazz.getSimpleName().charAt(0))
                        + clazz.getSimpleName().substring(1), mapper);
            }
        }

    }

    private void initSqlSessionFactories(ConfigurableListableBeanFactory beanFactory) throws Exception {
        Map<String, SqlSessionFactory> sqlSessionFactories = new HashMap<>(this.dataSourceLookup.getMapping().size());

        ReadWriteSplittingDataSource defaultDataSource = null;
        SqlSessionFactory defaultSqlSessionFactory = null;
        for (ReadWriteSplittingDataSource dataSource : this.dataSourceLookup.getMapping().values()) {

            SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
            sessionFactoryBean.setMapperLocations(mapperLocations);
            sessionFactoryBean.setDataSource(dataSource);
            sessionFactoryBean.setTypeAliasesPackage(this.packageName + ".domain.entity");

            // init 初始化所有sql对应的元数据、资源（sqlNode, sqlSource, mappedStatement）等
            sessionFactoryBean.afterPropertiesSet();

            if (defaultDataSource == null) {
                //第一个
                defaultDataSource = dataSource;
                defaultSqlSessionFactory = sessionFactoryBean.getObject();
            } else {
                SqlSessionFactory newSqlSessionFactory = sessionFactoryBean.getObject();
                Field conf = newSqlSessionFactory.getClass().getDeclaredField("configuration");
                conf.setAccessible(true);
                Configuration newConfiguration = (Configuration) conf.get(newSqlSessionFactory);
                Field mappedStatementField = newConfiguration.getClass().getDeclaredField("mappedStatements");

                //去掉final修饰符
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt( mappedStatementField,  mappedStatementField.getModifiers() & ~Modifier.FINAL);
                 mappedStatementField.setAccessible(true);

                //后续的元数据复用
                Configuration defaultConfiguration = defaultSqlSessionFactory.getConfiguration();
                Map<String, MappedStatement> reUsedMappedStatement = (Map)  mappedStatementField.get(defaultConfiguration);
                mappedStatementField.set(newConfiguration, reUsedMappedStatement);
            }
            beanFactory.registerSingleton(dataSource.getName() + "SqlSessionFactory", sessionFactoryBean);
            sqlSessionFactories.put(dataSource.getName(), sessionFactoryBean.getObject());
            defaultSqlSessionFactory = sessionFactoryBean.getObject();
        }

        this.sqlSessionFactoryLookup = new SqlSessionFactoryLookup(sqlSessionFactories);
    }

    private boolean isMapper(Class<?> clazz) {
        if (clazz.isInterface()) {
            return true;
        }
        return false;
    }

    private Object newMapper(final Class<?> clazz) {

        final Invoker invoker = new TShardingRoutingInvokeFactory(sqlSessionFactoryLookup).newInvoker(clazz);

        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                        return invoker.invoke(new DefaultInvocation(method, args));
                    }
                });
    }

    /**
     * 注入packageName配置
     *
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * 注入mapperLocations配置
     *
     * @param mapperLocations
     */
    public void setMapperLocations(Resource[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }
}


