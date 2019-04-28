package com.lintcheck.lintcheck.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lintcheck.lintcheck.R;

import java.util.List;

/*
 * Created by qianli.ma on 2019/4/18 0018.
 */
public class LintWidget extends RelativeLayout {

    private TextView tvLintCheck;

    public LintWidget(Context context) {
        this(context, null, 0);
    }

    public LintWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LintWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View inflate = View.inflate(context, R.layout.lintcheck_layout, this);
        tvLintCheck = inflate.findViewById(R.id.tv_lintcheck);
    }

    /**
     * 显示规范检查结果
     *
     * @param lintcodes 错误码集合
     */
    public void setLintTip(List<Integer> lintcodes) {
        // 1.检索集合中的检查码
        StringBuilder builder = new StringBuilder();
        int size = lintcodes.size();
        for (int i = 0; i < size; i++) {
            int lintcode = lintcodes.get(i);
            if (lintcode == LintHelper.IS_DIALOG_CLASS_EXIST) {
                builder.append(i).append("-").append("检测到工程中使用了android.app.Dialog子类,请改用普通layout");

            } else if (lintcode == LintHelper.IS_GLOBAL_VARIABLE_DIALOG_EXIST) {
                builder.append(i).append("-").append("检测到工程中使用了android.app.Dialog全局变量,请修改为局部变量");

            } else if (lintcode == LintHelper.IS_METHOD_PARAM_DIALOG_EXIST) {
                builder.append(i).append("-").append("检测到方法中使用了android.app.Dialog参数,请删除");

            } else if (lintcode == LintHelper.NOT_RULE_DIR_EXIST) {
                builder.append(i).append("-").append("检测到工程中存在非规定外的文件夹, 请删除");
                builder.append("\n");
                builder.append("工程中仅仅允许存在\n[adapter]\n[bean]\n[helper]\n[test]\n[ue]\n[utils]\n[widget]\n[wxapi]等以上目录");

            } else if (lintcode == LintHelper.NOT_END_WITH_HELPER) {
                builder.append(i).append("-").append("检测到工程helper目录中的文件非[Helper]结尾");

            } else if (lintcode == LintHelper.NOT_END_WITH_PARAM_OR_BEAN) {
                builder.append(i).append("-").append("检测到工程bean目录中的文件非[Bean]或者[Param]结尾, 请修改");

            } else if (lintcode == LintHelper.NOT_END_WITH_WIDGET) {
                builder.append(i).append("-").append("检测到工程widget目录中的文件非[Widget]结尾, 请修改");

            } else if (lintcode == LintHelper.NOT_RULE_TARGET_DIR_IN_UE_EXIST) {
                builder.append(i).append("-").append("检测到工程三级ue目录存在非([activity] 或 [frag])的次级目录, 请删除");

            } else if (lintcode == LintHelper.NOT_RULE_TARGET_DIR_IN_OTHER_EXIST) {
                builder.append(i).append("-").append("检测到工程非ue目录下存在次级目录, 请删除");

            } else if (lintcode == LintHelper.DIR_NUM_OVER_COUNT) {
                builder.append(i).append("-").append("检测到工程中有个别类在非指定目录内, 请移动");

            } else if (lintcode == LintHelper.NOT_RULE_TARGET_FILE_IN_UE_EXIST) {
                builder.append(i).append("-").append("检测到工程ue文件夹内存在非规定外的文件, 请移动");
                builder.append("\n");
                builder.append("文件应在activity或frag目录下");

            } else if (lintcode == LintHelper.NOT_EXTEND_TARGET_ACTIVITY) {
                builder.append(i).append("-").append("检测到工程activity目录下文件没有继承框架, 请继承");

            } else if (lintcode == LintHelper.NOT_EXTEND_TARGET_FRAG) {
                builder.append(i).append("-").append("检测到工程frag目录下文件没有继承框架, 请继承");
                
            } else if (lintcode == LintHelper.NOT_END_WITH_ADAPTER) {
                builder.append(i).append("-").append("检测到工程adapter目录中的文件非[Adapter]结尾");
                
            } else if (lintcode == LintHelper.NOT_SERIALIZABLE_IN_BEAN) {
                builder.append(i).append("-").append("检测到工程bean目录中的实体没有实现Serializable接口");
            }

            if (lintcode != LintHelper.DEFAULT_MATCH) {
                builder.append("\n\n");
            }
        }

        // 2.显示
        tvLintCheck.setText(builder.toString());
    }
}
