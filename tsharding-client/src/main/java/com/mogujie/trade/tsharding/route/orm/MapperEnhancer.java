package com.mogujie.trade.tsharding.route.orm;

import com.mogujie.trade.db.MapperRoutingHandler;
import com.mogujie.trade.tsharding.client.ShardingCaculator;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用Mapper增强基类，扩展Mapper sql时需要继承该类
 *
 * @author qigong on 5/1/15
 */
public abstract class MapperEnhancer {

    private static ClassPool pool = ClassPool.getDefault();

    private Map<String, Method> methodMap = new HashMap<>();
    private Map<String, MapperRoutingHandler> mapperHandlerMap = new HashMap<>();
    private Class<?> mapperClass;

    public MapperEnhancer(Class<?> mapperClass) {
        this.mapperClass = mapperClass;
    }

    /**
     * 代码增加方法标记
     *
     * @param record
     */
    public String enhancedShardingSQL(Object record) {
        return "enhancedShardingSQL";
    }

    public MapperEnhancer() {
        super();
    }

    /**
     * 对mapper进行增强，生成新的mapper，并主动加载新mapper类到classloader
     *
     * @param mapperClassName
     */
    public static void enhanceMapperClass(String mapperClassName, MapperRoutingHandler mapperRoutingHandler) throws Exception {

        Class originClass = Class.forName(mapperClassName);
        Method[] originMethods = originClass.getDeclaredMethods();

        CtClass cc = pool.get(mapperClassName);
        int tableSize = mapperRoutingHandler.tableSize();

        for (CtMethod ctMethod : cc.getDeclaredMethods()) {
            CtClass enhanceClass = pool.makeInterface(mapperClassName + "Sharding" + ctMethod.getName());
            for (long i = 0L; i < tableSize; i++) {
                CtMethod newMethod = new CtMethod(ctMethod.getReturnType(), ctMethod.getName() + mapperRoutingHandler.calculateTableIndex(i), ctMethod.getParameterTypes(), enhanceClass);
                Method method = getOriginMethod(newMethod, originMethods, mapperRoutingHandler);
                if (method.getParameterAnnotations()[0].length > 0) {
                    ClassFile ccFile = enhanceClass.getClassFile();
                    ConstPool constPool = ccFile.getConstPool();

                    //拷贝注解信息和注解内容，以支持mybatis mapper类的动态绑定
                    newMethod.getMethodInfo().addAttribute(MapperAnnotationEnhancer.duplicateParameterAnnotationsAttribute(constPool, method));
                }
                enhanceClass.addMethod(newMethod);
            }
            Class<?> loadThisClass = enhanceClass.toClass();

            //2015.09.22后不再输出类到本地
//            enhanceClass.writeFile(".");
        }
    }

    private static Method getOriginMethod(CtMethod ctMethod, Method[] originMethods, MapperRoutingHandler mapperRoutingHandler) {
        for (Method method : originMethods) {
            if (mapperRoutingHandler.methodOriginName(ctMethod.getName()).equals(method.getName())) {
                return method;
            }
        }
        throw new RuntimeException("enhanceMapperClass find method error!");
    }

    /**
     * 添加映射方法
     *
     * @param methodName
     * @param method
     */
    public void addMethodMap(String methodName, MapperRoutingHandler mapperRoutingHandler, Method method) {
        methodMap.put(methodName, method);
        mapperHandlerMap.put(methodName, mapperRoutingHandler);
    }

    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    /**
     * 反射对象，增加对低版本Mybatis的支持
     *
     * @param object 反射对象
     * @return
     */
    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }

    /**
     * 是否支持该通用方法
     *
     * @param msId
     * @return
     */
    public boolean supportMethod(String msId) {
        Class<?> mapperClass = getMapperClass(msId);
        if (this.mapperClass.isAssignableFrom(mapperClass)) {
            String methodName = getMethodName(msId);
            return methodMap.get(methodName) != null;
        }
        return false;
    }

    /**
     * 重新设置SqlSource
     *
     * @param ms
     * @param sqlSource
     */
    protected void setSqlSource(MappedStatement ms, SqlSource sqlSource) {
        MetaObject msObject = forObject(ms);
        msObject.setValue("sqlSource", sqlSource);
    }

    /**
     * 重新设置SqlSource
     *
     * @param ms
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public void setSqlSource(MappedStatement ms, Configuration configuration) throws Exception {
        String key = getMethodName(ms);
        Method method = methodMap.get(key);
        MapperRoutingHandler mapperRoutingHandler = mapperHandlerMap.get(key);
        try {
            if (method.getReturnType() == Void.TYPE) {
                method.invoke(this, ms);
            } else if (SqlSource.class.isAssignableFrom(method.getReturnType())) {
                // 按tableSize个数进行增强
                for (long i = 0; i < mapperRoutingHandler.tableSize(); i++) {
                    //新的带sharding的sql
                    SqlSource sqlSource = (SqlSource) method.invoke(this, ms, configuration, mapperRoutingHandler, i);

                    String newMsId = ms.getId() + ShardingCaculator.getNumberWithZeroSuffix(i);
                    newMsId = newMsId.replace("Mapper.", "MapperSharding" + getMethodName(ms) + ".");

                    //添加到ms库中
                    MappedStatement newMs = copyFromMappedStatement(ms, sqlSource, newMsId);
                    configuration.addMappedStatement(newMs);
                    setSqlSource(newMs, sqlSource);
                }
            } else {
                throw new RuntimeException("自定义Mapper方法返回类型错误,可选的返回类型为void和SqlNode!");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException() != null ? e.getTargetException() : e);
        }
    }

    protected MappedStatement copyFromMappedStatement(MappedStatement ms,
                                                      SqlSource newSqlSource, String newMsId) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), newMsId, newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        // setStatementTimeout()
        builder.timeout(ms.getTimeout());
        // setParameterMap()
        builder.parameterMap(ms.getParameterMap());
        // setStatementResultMap()
        List<ResultMap> resultMaps = ms.getResultMaps();
        builder.resultMaps(resultMaps);
        builder.resultSetType(ms.getResultSetType());
        // setStatementCache()
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());
        return builder.build();
    }

    /**
     * 根据msId获取接口类
     *
     * @param msId
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> getMapperClass(String msId) {
        String mapperClassStr = msId.substring(0, msId.lastIndexOf("."));
        try {
            return Class.forName(mapperClassStr);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法获取Mapper接口信息:" + msId);
        }
    }

    /**
     * 获取执行的方法名
     *
     * @param ms
     * @return
     */
    public static String getMethodName(MappedStatement ms) {
        return getMethodName(ms.getId());
    }

    /**
     * 获取执行的方法名
     *
     * @param msId
     * @return
     */
    public static String getMethodName(String msId) {
        return msId.substring(msId.lastIndexOf(".") + 1);
    }
}
