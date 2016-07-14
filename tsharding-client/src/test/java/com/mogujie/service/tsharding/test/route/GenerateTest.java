package com.mogujie.service.tsharding.test.route;

import com.mogujie.trade.tsharding.annotation.parameter.ShardingOrderPara;
import com.mogujie.trade.tsharding.route.orm.MapperAnnotationEnhancer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

import java.io.IOException;

/**
 * @author qigong on 15/9/5 下午12:14.
 */
public class GenerateTest {

    public static void main(String[] args) {

        ClassPool cp = ClassPool.getDefault();
        try {
            CtClass cc = cp.makeInterface("com.mogujie.service.tsharding.route.Tester");

            ClassPool pool = ClassPool.getDefault();

            Class originClass  = Class.forName("com.mogujie.service.tsharding.route.InvokerTest");


            CtClass c = pool.get("com.mogujie.service.tsharding.route.InvokerTest");
            CtMethod ctMethod = c.getDeclaredMethods()[0];
            CtMethod cm = new CtMethod(ctMethod.getReturnType(), ctMethod.getName(), ctMethod.getParameterTypes(), cc);
            ClassFile ccFile = cc.getClassFile();
            ConstPool constPool = ccFile.getConstPool();
            cm.getMethodInfo().addAttribute(MapperAnnotationEnhancer.duplicateParameterAnnotationsAttribute(constPool, originClass.getDeclaredMethods()[0]));

            cc.addMethod(cm);
            Class<?> newClass = cc.toClass();

            java.lang.annotation.Annotation[][] annotations = newClass.getDeclaredMethods()[0].getParameterAnnotations();
            if (annotations.length > 0) {
                for (int i = 0; i < annotations.length; i++) {
                    for (int j = 0; j < annotations[i].length; j++) {
                        if (annotations[i][j] instanceof ShardingOrderPara) {
                            Long shardingPara = 0L;
                            System.out.println("comin again!");
                            break;
                        }
                    }
                }
            }

            try {
                cc.writeFile(".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
