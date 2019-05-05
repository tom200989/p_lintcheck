package com.lintcheck.lintcheck.helper;

import android.app.Dialog;
import android.text.TextUtils;

import com.lintcheck.lintcheck.core.CheckClassProxy;
import com.lintcheck.lintcheck.core.GlobalVariable;
import com.lintcheck.lintcheck.core.MethodVariable;
import com.lintcheck.lintcheck.utils.Lgg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by qianli.ma on 2019/4/17 0017.
 */
public class LintHelper {

    private static final String TAG = "LintHelper";// 日志

    protected static final int DEFAULT_MATCH = 0;// 默认通过
    protected static final int IS_DIALOG_CLASS_EXIST = 1;// 存在dialog子类
    protected static final int IS_GLOBAL_VARIABLE_DIALOG_EXIST = 2;// 全局变量dialog
    protected static final int IS_METHOD_PARAM_DIALOG_EXIST = 3;// 方法存在dialog参数
    protected static final int NOT_RULE_DIR_EXIST = 4;// 存在规范外的目录
    protected static final int NOT_END_WITH_HELPER = 5;// helper目录的文件不是以[Helper]结尾
    protected static final int NOT_END_WITH_PARAM_OR_BEAN = 6;// bean目录的文件不是以 [Bean] 或者 [Param] 结尾
    protected static final int NOT_END_WITH_WIDGET = 7;// widget目录的文件不是以[Widget]结尾
    protected static final int NOT_RULE_TARGET_DIR_IN_UE_EXIST = 8;// 第5级不是[Activity]或者[frag]目录(ue目录下)
    protected static final int NOT_RULE_TARGET_DIR_IN_OTHER_EXIST = 9;// 第5级不是[Activity]或者[frag]目录(非ue目录下)
    protected static final int DIR_NUM_OVER_COUNT = 10;// 文件级别低于5级或者超出6级
    protected static final int NOT_RULE_TARGET_FILE_IN_UE_EXIST = 11;// ue文件夹下的文件不在[activity]或者[frag]的文件夹内
    protected static final int NOT_EXTEND_TARGET_ACTIVITY = 12;// activity目录下的文件没有继承指定框架
    protected static final int NOT_EXTEND_TARGET_FRAG = 13;// frag目录下的文件没有继承指定框架
    protected static final int NOT_END_WITH_ADAPTER = 14;// adapter目录的文件不是以[Adapter]结尾
    protected static final int NOT_SERIALIZABLE_IN_BEAN = 15;// bean文件夹的实体没有实现序列化接口

    public static Class extendActivity;// 需要继承的Activity
    public static Class extendFragment;// 需要继承的Fragment

    /**
     * 是否通过规范检查(对外)
     *
     * @param lintcodes 检查码集合
     * @return T: 通过
     */
    public static boolean isPassLint(List<Integer> lintcodes) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isPassLint()");
        return isPassLintProxy(lintcodes);
    }

    /**
     * 是否通过规范检查(对外)
     *
     * @param lintcodes 检查码集合
     * @return T: 通过
     */
    private static boolean isPassLintProxy(List<Integer> lintcodes) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isPassLintProxy()");
        for (int lintcode : lintcodes) {
            if (lintcode != 0) {
                return false;
            }
        }
        return true;
    }


    /**
     * 获取Lint检查结果(对外)
     *
     * @param pkn 指定包名
     */
    public static List<Integer> getLintResult(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":getLintResult()--> pkn: " + pkn);
        return getLintProxy(pkn);
    }

    /**
     * 获取Lint检查结果
     *
     * @param pkn 指定包
     * @return 检测结果
     */
    private static List<Integer> getLintProxy(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":getLintProxy()--> pkn: " + pkn);
        // 0.准备收集不符合规范信息
        List<Integer> lintCodes = new ArrayList<>();
        // 1.检查是否存在Dialog类
        lintCodes.add(isDialogClassExist(pkn));
        // 2.检查全局变量是否存在Dialog
        lintCodes.add(isGlobalVariableDialogExist(pkn));
        // 3.检查方法参数是否存在Dialog
        lintCodes.add(isMethodParamDialogExist(pkn));
        // 4.文件层次是否超过限制
        lintCodes.addAll(isFolderMatch(pkn));
        // 5.整理重复元素
        lintCodes = filterRepeatCode(lintCodes);
        // 5.返回结果集合
        return lintCodes;
    }

    /* -------------------------------------------- private -------------------------------------------- */

    /**
     * 过滤重复元素
     *
     * @param lintCodes 原集合
     * @return 新集合
     */
    private static List<Integer> filterRepeatCode(List<Integer> lintCodes) {
        List<Integer> tempLints = new ArrayList<>();
        for (int lintCode : lintCodes) {
            if (!tempLints.contains(lintCode) & lintCode != 0) {
                tempLints.add(lintCode);
            }
        }
        return tempLints;
    }

    /**
     * 检查是否存在Dialog类
     *
     * @param pkn 指定包名
     * @return T:存在
     */
    private static int isDialogClassExist(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isDialogClassExist()--> pkn: " + pkn);
        List<Class> targetClasssProxy = CheckClassProxy.getTargetClasssProxy(pkn, Dialog.class);
        for (Class clazz : targetClasssProxy) {
            //Lgg.t(TAG).ee("有Dialog子类: " + clazz.getName());
        }
        return targetClasssProxy.size() > 0 ? IS_DIALOG_CLASS_EXIST : DEFAULT_MATCH;
    }


    /**
     * 检查全局变量是否存在Dialog
     *
     * @return T:存在
     */
    private static int isGlobalVariableDialogExist(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isGlobalVariableDialogExist()--> pkn: " + pkn);
        // 1.获取到每个类
        List<Class> allClass = CheckClassProxy.getAllClassProxy(pkn);
        // 2.检查每个类的所有全局变量
        for (Class<?> clazz : allClass) {
            List<GlobalVariable> globalVariables = CheckClassProxy.getGlobalVariableProxy(clazz);
            for (GlobalVariable globalVariable : globalVariables) {
                Class<?> type = globalVariable.type;
                if (Dialog.class.isAssignableFrom(type)) {
                    //Lgg.t(TAG).ee("全局变量有Dialog子类: " + clazz.getName() + "; 变量名: " + type.getSimpleName());
                    return IS_GLOBAL_VARIABLE_DIALOG_EXIST;
                }
            }
        }
        return DEFAULT_MATCH;
    }

    /**
     * 检查方法参数是否存在Dialog
     *
     * @param pkn 指定包名
     * @return T:存在
     */
    private static int isMethodParamDialogExist(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isMethodParamDialogExist()--> pkn: " + pkn);
        List<Class> allClass = CheckClassProxy.getAllClassProxy(pkn);
        for (Class clazz : allClass) {
            List<MethodVariable> methods = CheckClassProxy.getMethodsBody(clazz);
            for (MethodVariable method : methods) {
                Class[] params = method.params;
                if (params.length > 0) {
                    for (Class<?> param : params) {
                        if (Dialog.class.isAssignableFrom(param)) {
                            //Lgg.t(TAG).ee("方法参数有Dialog子类: " + clazz.getName() + "; 方法名: " + method.name + "; 参数: " + param
                            // .getSimpleName());
                            return IS_METHOD_PARAM_DIALOG_EXIST;
                        }
                    }
                }
            }
        }

        return DEFAULT_MATCH;
    }

    /**
     * 文件层次是否不符合规范
     *
     * @param pkn 指定包名
     * @return T:超过
     */
    @SuppressWarnings("unchecked")
    private static List<Integer> isFolderMatch(String pkn) {
        Lgg.t(TAG).ii("Method--> " + LintHelper.class.getSimpleName() + ":isFolderMatch()--> pkn: " + pkn);
        // -1.创建收集集合
        List<Integer> tempCodes = new ArrayList<>();
        // 0.获取所有类
        List<Class> allClass = CheckClassProxy.getAllClassProxy(pkn);
        // 0.1.获取每个类的全限命名
        for (Class<?> clazz : allClass) {

            // 0.1.1.过滤内部类以及［ViewBinding］类
            if (clazz.getName().contains("$") | clazz.getName().contains("ViewBinding")) {
                continue;
            }

            String[] arr = clazz.getName().split("\\.");
            int arrLength = arr.length;

            // 0.2.是否存在规范外的目录
            if (arrLength > 3) {
                Lgg.t(TAG).ii("file dir level :" + clazz.getName());
                String level4Dir = arr[3];// 4级目录

                // 0.2.1.过滤R2类
                if (level4Dir.contains("R2")) {
                    continue;
                }
                
                boolean isAdapterDir = level4Dir.equalsIgnoreCase("adapter");
                boolean isAppDir = level4Dir.equalsIgnoreCase("app");
                boolean isBeanDir = level4Dir.equalsIgnoreCase("bean");
                boolean isHelperDir = level4Dir.equalsIgnoreCase("helper");
                boolean isTestDir = level4Dir.equalsIgnoreCase("test");
                boolean isUeDir = level4Dir.equalsIgnoreCase("ue");
                boolean isUtilsDir = level4Dir.equalsIgnoreCase("utils");
                boolean isWidgetDir = level4Dir.equalsIgnoreCase("widget");
                boolean isWXDir = level4Dir.equalsIgnoreCase("wxapi");

                if (!isAdapterDir & !isAppDir & !isBeanDir & !isHelperDir & !isTestDir & !isUeDir & !isUtilsDir & !isWidgetDir & !isWXDir) {
                    //Lgg.t(TAG).ee("规范外的目录: " + clazz.getName());
                    tempCodes.add(NOT_RULE_DIR_EXIST);
                }
            }

            // 1.文件级别为5级或者6级--> 符合基本规范
            if (arrLength == 5 | arrLength == 6) {

                // 2.当 [等于5级] 时,检测如下
                // helper目录的文件是否以[helper]或者[Service]结尾
                // bean目录的文件是否以 [bean] 或者 [param] 结尾
                // widget目录的文件是否以[widget]结尾
                if (arr.length == 5) {
                    String level4Dir = arr[3];// 4级目录
                    String level4File = arr[4];// 5级文件
                    if (level4Dir.equalsIgnoreCase("helper")) {
                        if (!level4File.endsWith("Helper") & !level4File.endsWith("Service")) {
                            //Lgg.t(TAG).ee("不以[Helper]或者[Service]结尾: " + clazz.getName());
                            tempCodes.add(NOT_END_WITH_HELPER);
                        }
                    }
                    if (level4Dir.equalsIgnoreCase("bean")) {
                        if (!level4File.endsWith("Param") & !level4File.endsWith("Bean")) {
                            //Lgg.t(TAG).ee("不以 [Bean] 或者 [Param] 结尾: " + clazz.getName());
                            tempCodes.add(NOT_END_WITH_PARAM_OR_BEAN);
                        }
                        if (!Serializable.class.isAssignableFrom(clazz)) {
                            //Lgg.t(TAG).ee("bean文件夹的实体没有实现Serializable接口");
                            tempCodes.add(NOT_SERIALIZABLE_IN_BEAN);
                        }
                    }
                    if (level4Dir.equalsIgnoreCase("widget")) {
                        if (!level4File.endsWith("Widget")) {
                            //Lgg.t(TAG).ee("不以[widget]结尾: " + clazz.getName());
                            tempCodes.add(NOT_END_WITH_WIDGET);
                        }
                    }
                    if (level4Dir.equalsIgnoreCase("adapter")) {
                        if (!level4File.endsWith("Adapter") & !level4File.endsWith("Holder")) {
                            //Lgg.t(TAG).ee("不以[Adapter]或者[Holder]结尾: " + clazz.getName());
                            tempCodes.add(NOT_END_WITH_ADAPTER);
                        }
                    }

                    if (level4Dir.equalsIgnoreCase("ue")) {
                        //Lgg.t(TAG).ee("UE文件夹内存在不规范位置的文件: " + clazz.getName());
                        tempCodes.add(NOT_RULE_TARGET_FILE_IN_UE_EXIST);
                    }

                } else {
                    // 2.当 [等于6级] 时, 检查第5级是否为[Activity]或者[frag]目录
                    String level4Dir = arr[3];// 4级目录
                    String level5Dir = arr[4];// 5级目录
                    if (level4Dir.equalsIgnoreCase("ue")) {// UE目录下
                        boolean isActivityDir = level5Dir.equalsIgnoreCase("activity");
                        boolean isFragDir = level5Dir.equalsIgnoreCase("frag");
                        if (!isActivityDir & !isFragDir) {
                            //Lgg.t(TAG).ee("UE文件夹内存在其他目录: " + clazz.getName());
                            tempCodes.add(NOT_RULE_TARGET_DIR_IN_UE_EXIST);
                        }

                        // 判断[activity]目录下的文件是否都继承指定类
                        if (extendActivity != null & level5Dir.equalsIgnoreCase("activity")) {
                            if (!extendActivity.isAssignableFrom(clazz)) {
                                //Lgg.t(TAG).ee("activity文件夹下有问题的文件: " + clazz.getName());
                                tempCodes.add(NOT_EXTEND_TARGET_ACTIVITY);
                            }
                        }

                        // 判断[frag]目录下的文件是否都继承指定类
                        if (extendFragment != null & level5Dir.equalsIgnoreCase("frag")) {
                            if (!extendFragment.isAssignableFrom(clazz)) {
                                //Lgg.t(TAG).ee("frag夹文件下有问题的文件: " + clazz.getName());
                                tempCodes.add(NOT_EXTEND_TARGET_FRAG);
                            }
                        }

                    } else {// 非UE目录下--> 不允许存在次级目录
                        if (!TextUtils.isEmpty(level5Dir)) {
                            //Lgg.t(TAG).ee("非UE目录下存在次级目录: " + clazz.getName());
                            tempCodes.add(NOT_RULE_TARGET_DIR_IN_OTHER_EXIST);
                        }
                    }

                }

            } else {
                //Lgg.t(TAG).ee("文件级别低于5级或者超出6级: " + clazz.getName());
                tempCodes.add(DIR_NUM_OVER_COUNT);
            }
        }

        // 返回收集结果
        return tempCodes;
    }
}
