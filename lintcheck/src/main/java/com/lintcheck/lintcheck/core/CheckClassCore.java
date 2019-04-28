package com.lintcheck.lintcheck.core;

import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

/* 声明: 本类仅限于在 6.0 <= android X <= 8.1 */
/* 声明: android 6.0 = M */
/* 声明: android 8.1 = O */
public class CheckClassCore {

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 根据包名获取包名下所有的类
     *
     * @param pkn 包名
     * @return 包名下所有类
     */
    protected static List<Class> getAllClasss(String pkn) {
        return excludeRJava(getTargetClasss(pkn, null));
    }


    /**
     * 获取: 指定包下--> 指定目标类的子类
     * 如: TestDialog extend Dialog
     *
     * @param pkn         需要搜寻的包名
     * @param targetClass 目标类
     * @return 搜寻结果
     */
    protected static List<Class> getTargetClasss(String pkn, Class targetClass) {
        return excludeRJava(getClassCore(pkn, targetClass));
    }

    /**
     * 获取指定类的全局变量
     *
     * @param targetClass 目标类
     * @return 全局变量集合
     */
    protected static List<GlobalVariable> getGlobalVariable(@Nullable Class targetClass) {
        // 0.新建全局变量集合
        List<GlobalVariable> globalVariables = new ArrayList<>();
        if (targetClass == null) {
            return globalVariables;
        }
        // 1.获取全局变量字段
        Field[] globalFields = targetClass.getDeclaredFields();
        if (globalFields.length > 0) {
            for (Field globalField : globalFields) {
                // 2.获取全局变量的修饰域
                int modifiers = globalField.getModifiers();
                // 3.获取全局变量的类型
                Class<?> type = globalField.getType();
                // 4.获取全局变量的名称
                String name = globalField.getName();
                // 5.封装
                GlobalVariable globalVariable = new GlobalVariable();
                globalVariable.modifiers = modifiers;
                globalVariable.type = type;
                globalVariable.name = name;
                globalVariables.add(globalVariable);
            }
        }
        return globalVariables;
    }

    /**
     * 获取方法体集合
     *
     * @param targetClass 目标类
     * @return 方法体集合
     */
    protected static List<MethodVariable> getMethodsBody(Class targetClass) {
        // 0.新建方法体集合
        List<MethodVariable> methodVariables = new ArrayList<>();
        if (targetClass == null) {
            Log.e(CheckClassCore.class.getSimpleName(), "target class is not found");
            return methodVariables;
        }
        // 1.获取到方法体数组
        Method[] methods = targetClass.getDeclaredMethods();
        for (Method method : methods) {
            // 2.获取方法体修饰域
            int modifiers = method.getModifiers();
            // 3.获取方法体返回类型
            Class<?> returnType = method.getReturnType();
            // 4.获取方法体方法名
            String name = method.getName();
            // 5.获取参数列表
            Class<?>[] params = method.getParameterTypes();
            // 6.封装
            MethodVariable methodVariable = new MethodVariable();
            methodVariable.modifiers = modifiers;
            methodVariable.returnType = returnType;
            methodVariable.name = name;
            methodVariable.params = params;
            methodVariables.add(methodVariable);
        }
        return methodVariables;
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 核心方法 (@call by getTargetClasss())
     *
     * @param pkn         包名
     * @param targetClass 搜寻的目标class
     * @return 搜寻结果
     */
    private static List<Class> getClassCore(String pkn, @Nullable Class<?> targetClass) {

        List<Class> targetClassList = new ArrayList<>();
        List<DexFile> dexFiles = new ArrayList<>();
        try {
            // 1.获取当前线程的类加载器
            BaseDexClassLoader classLoader = ((BaseDexClassLoader) Thread.currentThread().getContextClassLoader());
            Field pathListField = Objects.requireNonNull(classLoader.getClass().getSuperclass()).getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(classLoader);
            // 2.获取dex预编译字段
            Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object dexElements = dexElementsField.get(pathList);
            int dexLength = Array.getLength(dexElements);
            Field dexFileField = null;
            // 3.遍历查询带有声明［dexFile］的字段
            for (int i = 0; i < dexLength; i++) {
                Object dexElement = Array.get(dexElements, i);
                if (dexFileField == null) {
                    dexFileField = dexElement.getClass().getDeclaredField("dexFile");
                    dexFileField.setAccessible(true);
                }
                DexFile dexFile = (DexFile) dexFileField.get(dexElement);
                if (dexFile != null) {
                    dexFiles.add(dexFile);
                }
            }
            // 4.使用指定目标搜寻
            for (DexFile file : dexFiles) {
                for (Enumeration<String> entries = file.entries(); entries.hasMoreElements(); ) {
                    final String ele = entries.nextElement();
                    if (ele.contains(pkn)) {
                        if (targetClass != null) {// 5.如果指定目标--> 则进行条件匹配
                            if (targetClass.isAssignableFrom(Class.forName(ele))) {
                                targetClassList.add(Class.forName(ele));
                            }
                        } else {// 5.如果不指定目标--> 则直接添加
                            targetClassList.add(Class.forName(ele));
                        }
                    }
                }
            }
            // 6.打印个数
            Log.i(CheckClassCore.class.getSimpleName(), "target class size: " + Integer.toString(targetClassList.size()));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(CheckClassCore.class.getSimpleName(), "error : " + e.getMessage());
        }
        return targetClassList;
    }

    /**
     * 排除R文件类
     *
     * @param targetClass 目标集合
     * @return 排除R文件的集合
     */
    private static List<Class> excludeRJava(List<Class> targetClass) {
        List<Class> tempClasss = new ArrayList<>();
        for (Class target : targetClass) {
            String className = target.getName();
            String simpleName = target.getSimpleName();
            // 排除R文件类以及其内部类
            if (className.contains("R$") | simpleName.equals("R") | className.contains("BuildConfig")) {
                continue;
            }
            tempClasss.add(target);
        }
        return tempClasss;
    }

}
