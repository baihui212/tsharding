package com.mogujie.trade.tsharding.route.orm;

import com.mogujie.trade.db.DataSourceRouting;
import com.mogujie.trade.db.MapperRoutingHandler;
import com.mogujie.trade.tsharding.annotation.ShardingExtensionMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Mapper处理主要逻辑，最关键的一个类
 * <p/>
 * <p>
 * 参考项目地址 : <a href="https://github.com/abel533/Mapper"
 * target="_blank">https://github.com/abel533/Mapper</a>
 * </p>
 *
 * @author qigong on 5/1/15
 */
public class MapperHelperForSharding {

    /**
     * 注册的通用Mapper接口
     */
    private Map<Class<?>, MapperEnhancer> registerMapper = new HashMap<Class<?>, MapperEnhancer>();

    /**
     * 缓存msid和MapperTemplate
     */
    private Map<String, MapperEnhancer> msIdCache = new HashMap<String, MapperEnhancer>();
    /**
     * 缓存skip结果
     */
    private final Map<String, Boolean> msIdSkip = new HashMap<String, Boolean>();

    /**
     * 缓存已经处理过的Collection<MappedStatement>
     */
    private Set<Collection<MappedStatement>> collectionSet = new HashSet<Collection<MappedStatement>>();

    /**
     * 是否使用的Spring
     */
    private boolean spring = false;

    /**
     * 是否为Spring4.x以上版本
     */
    private boolean spring4 = false;

    /**
     * Spring版本号
     */
    private String springVersion;

    /**
     * 缓存初始化时的SqlSession
     */
    private List<SqlSession> sqlSessions = new ArrayList<SqlSession>();

//    /**
//     * 用于保存MapperShardingInitializer中的needEnhancedClasses值,将其增加mapper方法移到此处,目的是为了知道需要增加多少个mapper
//     */
//    private String needEnhancedClasses;
//
//    public MapperHelperForSharding(String needEnhancedClasses) {
//        this.needEnhancedClasses = needEnhancedClasses;
//    }

    /**
     * 针对Spring注入需要处理的SqlSession
     *
     * @param sqlSessions
     */
    public void setSqlSessions(SqlSession[] sqlSessions) {
        if (sqlSessions != null && sqlSessions.length > 0) {
            this.sqlSessions.addAll(Arrays.asList(sqlSessions));
        }
    }

    /**
     * Spring初始化方法，使用Spring时需要配置init-method="initMapper"
     */
    public void initMapper() {
        // 只有Spring会执行这个方法,所以Spring配置的时候,从这儿可以尝试获取Spring的版本
        // 先判断Spring版本,对下面的操作有影响
        // Spring4以上支持泛型注入,因此可以扫描通用Mapper
        if (!initSpringVersion()) {
            throw new RuntimeException("Error! Spring4 is necessary!");
        }

        for (SqlSession sqlSession : sqlSessions) {
            processConfiguration(sqlSession.getConfiguration());
        }
    }

    /**
     * 检测Spring版本号,Spring4.x以上支持泛型注入
     */
    private boolean initSpringVersion() {
        try {
            // 反射获取SpringVersion
            Class<?> springVersionClass = Class.forName("org.springframework.core.SpringVersion");
            springVersion = (String) springVersionClass.getDeclaredMethod("getVersion", new Class<?>[0]).invoke(null,
                    new Object[0]);
            spring = true;
            if (springVersion.indexOf(".") > 0) {
                int MajorVersion = Integer.parseInt(springVersion.substring(0, springVersion.indexOf(".")));
                if (MajorVersion > 3) {
                    spring4 = true;
                } else {
                    spring4 = false;
                }
            }
        } catch (Exception e) {
            spring = false;
            spring4 = false;
        }
        return spring && spring4;
    }

    /**
     * 保存已增强处理过的Mapper
     */
    private HashMap<String, MapperRoutingHandler> mapperHasEnhance = new HashMap<>();

    /**
     * 通过通用Mapper接口获取对应的MapperTemplate
     *
     * @param mapperClass
     */
    private MapperEnhancer fromMapperClass(Class<?> mapperClass) {
        if (!mapperHasEnhance.containsKey(mapperClass.getName())) {
            // 说明还没处理
            DataSourceRouting dataSourceRouting = mapperClass.getAnnotation(DataSourceRouting.class);
            try {
                MapperRoutingHandler shardingMapper = dataSourceRouting.handler().newInstance();
                MapperEnhancer.enhanceMapperClass(mapperClass.getName(), shardingMapper);
                mapperHasEnhance.put(mapperClass.getName(), shardingMapper);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Method[] methods = mapperClass.getDeclaredMethods();
        Class<?> templateClass = null;
        Class<?> tempClass = null;
        Map<String, MapperRoutingHandler> mapperHandlerMap = new HashMap<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ShardingExtensionMethod.class)) {
                ShardingExtensionMethod annotation = method.getAnnotation(ShardingExtensionMethod.class);
                tempClass = annotation.type();
                mapperHandlerMap.put(method.getName(), mapperHasEnhance.get(mapperClass.getName()));
            }
            if (templateClass == null) {
                templateClass = tempClass;
            } else if (templateClass != tempClass) {
                throw new RuntimeException("一个通用Mapper中只允许存在一个MapperTemplate子类!");
            }
        }
        if (templateClass == null || !MapperEnhancer.class.isAssignableFrom(templateClass)) {
            throw new RuntimeException("接口中不存在包含type为MapperTemplate的Provider注解，这不是一个合法的通用Mapper接口类!");
        }
        MapperEnhancer mapperEnhancer;
        try {
            mapperEnhancer = (MapperEnhancer) templateClass.getConstructor(Class.class).newInstance(mapperClass);
        } catch (Exception e) {
            throw new RuntimeException("实例化MapperTemplate对象失败:" + e.getMessage(), e);
        }
        // 注册方法
        for (Map.Entry<String, MapperRoutingHandler> item : mapperHandlerMap.entrySet()) {
            try {
                mapperEnhancer.addMethodMap(item.getKey(), item.getValue(), templateClass.getMethod("enhancedShardingSQL", MappedStatement.class, Configuration.class, MapperRoutingHandler.class, Long.class));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(templateClass.getCanonicalName() + "中缺少enhancedShardingSQL方法!");
            }
        }
        return mapperEnhancer;
    }

    /**
     * 注册通用Mapper接口
     *
     * @param mapperClass
     * @throws Exception
     */
    public void registerMapper(Class<?> mapperClass) {
        if (registerMapper.get(mapperClass) == null) {
            MapperEnhancer enhancer = fromMapperClass(mapperClass);
            registerMapper.put(mapperClass, enhancer);
        } else {
            throw new RuntimeException("已经注册过的通用Mapper[" + mapperClass.getCanonicalName() + "]不能多次注册!");
        }
    }

    /**
     * 注册通用Mapper接口
     *
     * @param mapperClass
     * @throws Exception
     */
    public void registerMapper(String mapperClass) {
        try {
            registerMapper(Class.forName(mapperClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("注册通用Mapper[" + mapperClass + "]失败，找不到该通用Mapper!");
        }
    }

    /**
     * 方便Spring注入
     *
     * @param mappers
     */
    public void setMappers(String[] mappers) {
        if (mappers != null && mappers.length > 0) {
            for (String mapper : mappers) {
                registerMapper(mapper);
            }
        }
    }

    /**
     * 判断当前的接口方法是否需要进行拦截
     *
     * @param msId
     * @return
     */
    public boolean isMapperMethod(String msId) {
        if (msIdSkip.get(msId) != null) {
            return msIdSkip.get(msId);
        }
        for (Map.Entry<Class<?>, MapperEnhancer> entry : registerMapper.entrySet()) {
            if (entry.getValue().supportMethod(msId)) {
                msIdSkip.put(msId, true);
                return true;
            }
        }
        msIdSkip.put(msId, false);
        return false;
    }

    /**
     * 获取MapperTemplate
     *
     * @param msId
     * @return
     */
    private MapperEnhancer getMapperTemplate(String msId) {
        MapperEnhancer mapperEnhancer = null;
        if (msIdCache.get(msId) != null) {
            mapperEnhancer = msIdCache.get(msId);
        } else {
            for (Map.Entry<Class<?>, MapperEnhancer> entry : registerMapper.entrySet()) {
                if (entry.getValue().supportMethod(msId)) {
                    mapperEnhancer = entry.getValue();
                    break;
                }
            }
            msIdCache.put(msId, mapperEnhancer);
        }
        return mapperEnhancer;
    }

    /**
     * 重新设置SqlSource
     *
     * @param ms
     */
    public void setSqlSource(MappedStatement ms, Configuration configuration) {
        MapperEnhancer mapperEnhancer = getMapperTemplate(ms.getId());
        try {
            if (mapperEnhancer != null) {
                mapperEnhancer.setSqlSource(ms, configuration);
            }
        } catch (Exception e) {
            throw new RuntimeException("调用方法异常:" + e.getMessage(), e);
        }
    }

    /**
     * 处理configuration中全部的MappedStatement
     *
     * @param configuration
     */
    public void processConfiguration(Configuration configuration) {
        Collection<MappedStatement> collection = configuration.getMappedStatements();
        // 防止反复处理一个
        if (collectionSet.contains(collection)) {
            return;
        } else {
            collectionSet.add(collection);
        }

        Collection<MappedStatement> tmpCollection = new HashSet<>();
        tmpCollection.addAll(collection);

        Iterator<MappedStatement> iterator = tmpCollection.iterator();
        while (iterator.hasNext()) {
            Object object = iterator.next();
            if (object instanceof MappedStatement) {
                MappedStatement ms = (MappedStatement) object;
                if (isMapperMethod(ms.getId())) {
                    setSqlSource(ms, configuration);
                }
            }
        }
    }
}