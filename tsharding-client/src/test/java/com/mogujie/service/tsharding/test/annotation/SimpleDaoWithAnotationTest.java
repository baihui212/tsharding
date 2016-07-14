package com.mogujie.service.tsharding.test.annotation;

import com.alibaba.fastjson.JSON;
import com.mogujie.trade.tsharding.annotation.parameter.ShardingBuyerPara;
import org.apache.ibatis.annotations.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @auther qigong on 5/29/15 12:46 PM.
 */
public class SimpleDaoWithAnotationTest {

    private static ShardingBuyerPara a;

    public static void main(String[] args) {
        Class z = SimpleDao.class;
        try {
            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("proxy method");
                    System.out.println(JSON.toJSONString(method.getParameterTypes()));
                    Annotation[][] an = method.getParameterAnnotations();
                    if (an.length > 0) {
                        for (int i = 0; i < an.length; i++) {
                            System.out.print("i::" + i + ";");
                            System.out.println(JSON.toJSONString(an[i].length));
                            for (int j = 0; j < an[i].length; j++) {
                                System.out.println("j::" + j);
                                if(j==0) {
                                    a = (ShardingBuyerPara) an[i][j];
                                    System.out.println("sharding参数"+args[i]);
                                }else {
                                    Param b = (Param) an[i][j];
                                    System.out.println(b.value());
                                }
                            }
                        }
                    }
                    return method.invoke(SimpleDaoImpl.class.newInstance(), new Object[]{"abc", "cde", 4191574207234L});
                }
            };
            Proxy proxy = (Proxy) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{SimpleDao.class}, handler);

            Method m = z.getMethod("abc", new Class[]{String.class, String.class, Long.class});
            try {
                handler.invoke(proxy, m, new Object[]{"abc", "cde", 4191574207234L});
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
