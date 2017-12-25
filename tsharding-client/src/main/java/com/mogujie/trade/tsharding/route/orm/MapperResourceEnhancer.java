package com.mogujie.trade.tsharding.route.orm;

import com.mogujie.trade.db.MapperRoutingHandler;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Mappper sql增强
 *
 * @author qigong on 5/1/15
 */
public class MapperResourceEnhancer extends MapperEnhancer {

    Logger logger = LoggerFactory.getLogger(MapperResourceEnhancer.class);

    public MapperResourceEnhancer(Class<?> mapperClass) {
        super(mapperClass);
    }

    public SqlSource enhancedShardingSQL(MappedStatement ms, Configuration configuration, MapperRoutingHandler mapperRoutingHandler, Long shardingPara) {

        String tableName = mapperRoutingHandler.newTableName(shardingPara);
        String baseTableName = mapperRoutingHandler.baseTableName();
        SqlSource result = null;

        try {
            if (ms.getSqlSource() instanceof DynamicSqlSource) {

                DynamicSqlSource sqlSource = (DynamicSqlSource) ms.getSqlSource();
                Class sqlSourceClass = sqlSource.getClass();
                Field sqlNodeField = sqlSourceClass.getDeclaredField("rootSqlNode");
                sqlNodeField.setAccessible(true);
                MixedSqlNode rootSqlNode = (MixedSqlNode) sqlNodeField.get(sqlSource);
                Class mixedSqlNodeClass = rootSqlNode.getClass();
                Field contentsField = mixedSqlNodeClass.getDeclaredField("contents");
                contentsField.setAccessible(true);
                List<SqlNode> textSqlNodes = (List<SqlNode>) contentsField.get(rootSqlNode);

                List<SqlNode> newSqlNodesList = new ArrayList();
                //StaticTextSqlNode
                Class textSqlNodeClass = textSqlNodes.get(0).getClass();
                Field textField = textSqlNodeClass.getDeclaredField("text");
                textField.setAccessible(true);
                for (SqlNode node : textSqlNodes) {
                    if (node instanceof StaticTextSqlNode) {
                        StaticTextSqlNode textSqlNode = (StaticTextSqlNode) node;
                        String text = (String) textField.get(textSqlNode);
                        if (!text.contains(baseTableName)) {
                            newSqlNodesList.add(node);
                        } else {
                            newSqlNodesList.add(new StaticTextSqlNode(replaceWithShardingTableName(text, baseTableName, tableName)));
                        }
                    } else {
                        newSqlNodesList.add(node);
                    }
                }

                MixedSqlNode newrootSqlNode = new MixedSqlNode(newSqlNodesList);
                result = new DynamicSqlSource(configuration, newrootSqlNode);
                return result;

            } else if (ms.getSqlSource() instanceof RawSqlSource) {

                RawSqlSource sqlSource = (RawSqlSource) ms.getSqlSource();
                Class sqlSourceClass = sqlSource.getClass();
                Field sqlSourceField = sqlSourceClass.getDeclaredField("sqlSource");
                sqlSourceField.setAccessible(true);
                StaticSqlSource staticSqlSource = (StaticSqlSource) sqlSourceField.get(sqlSource);
                Field sqlField = staticSqlSource.getClass().getDeclaredField("sql");
                Field parameterMappingsField = staticSqlSource.getClass().getDeclaredField("parameterMappings");
                sqlField.setAccessible(true);
                parameterMappingsField.setAccessible(true);

                //sql处理
                String sql = (String) sqlField.get(staticSqlSource);

                if (!sql.contains(baseTableName)) {
                    result = sqlSource;
                } else {
                    sql = replaceWithShardingTableName(sql, baseTableName, tableName);
                    result = new RawSqlSource(configuration, sql, null);
                    //为sqlSource对象设置mappering参数
                    StaticSqlSource newStaticSqlSource = (StaticSqlSource) sqlSourceField.get(result);
                    List<ParameterMapping> parameterMappings = (List<ParameterMapping>) parameterMappingsField.get(staticSqlSource);
                    parameterMappingsField.set(newStaticSqlSource, parameterMappings);
                }
                return result;
            } else {
                throw new RuntimeException("wrong sqlSource type!" + ms.getResource());
            }

        } catch (Exception e) {
            logger.error("reflect error!, ms resources:" + ms.getResource(), e);
        }
        return result;
    }

    /**
     * 将sql中表名进行替换
     *
     * @param text
     * @param baseTableName
     * @param tableName
     * @return
     */
    private String replaceWithShardingTableName(String text, String baseTableName, String tableName) {
        return text.replace(baseTableName, tableName);
    }
}
