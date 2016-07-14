package com.mogujie.service.tsharding.test.route;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author qigong on 15/9/5 下午12:14.
 */
public class TestJavaAssit {

    public static void main(String[] args){

        ClassPool cp = ClassPool.getDefault();
        try {
            CtClass cc = cp.makeClass("com.mogujie.service.tsharding.route.Tester1");
            CtMethod cm = new CtMethod(CtClass.intType, "add", new CtClass[] {
                    CtClass.intType, CtClass.intType }, cc);


            ClassFile ccFile = cc.getClassFile();
            ConstPool constpool = ccFile.getConstPool();

            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation annot = new Annotation("com.mogujie.trade.tsharding.annotation.parameter.ShardingTablePara", constpool);
            annot.addMemberValue("value", new IntegerMemberValue(ccFile.getConstPool(), 0));

            cm.getMethodInfo().addAttribute(attr);

//
            cm.setBody("return $1 + $2;");
            cm.insertAfter("for(int i=0;i<$args.length;i++)"
                    + "{System.out.println(\"args[\"+i+\"]=\"+$args[i]);}");
            cc.addMethod(cm);
            Class<?> newClass = cc.toClass();
            Object o = newClass.newInstance();
            Method m = newClass.getDeclaredMethod("add", int.class, int.class);
            System.out.println("Result:" + m.invoke(o, 1, 2));
            try {
                cc.writeFile(".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
