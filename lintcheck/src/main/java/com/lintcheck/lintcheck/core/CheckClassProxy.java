package com.lintcheck.lintcheck.core;

import java.util.List;

/*
 * Created by qianli.ma on 2019/4/17 0017.
 */
public class CheckClassProxy {

    /**
     * 获取指定包下所有类对象
     *
     * @param pkn 指定包名
     * @return 类对象集合
     */
    public static List<Class> getAllClassProxy(String pkn) {
        return CheckClassCore.getAllClasss(pkn);
    }

    /**
     * 获取指定类的全局变量
     *
     * @param targetClass 指定类
     * @return 全局变量集合
     */
    public static List<GlobalVariable> getGlobalVariableProxy(Class targetClass) {
        return CheckClassCore.getGlobalVariable(targetClass);
    }

    /**
     * 获取: 指定包下--> 指定目标类的子类
     * 如: TestDialog extend Dialog
     *
     * @param pkn         需要搜寻的包名
     * @param targetClass 目标类
     * @return 搜寻结果
     */
    public static List<Class> getTargetClasssProxy(String pkn, Class targetClass) {
        return CheckClassCore.getTargetClasss(pkn, targetClass);
    }

    /**
     * 获取方法体集合
     *
     * @param targetClass 目标类
     * @return 方法体集合
     */
    public static List<MethodVariable> getMethodsBody(Class targetClass) {
        return CheckClassCore.getMethodsBody(targetClass);
    }
}
