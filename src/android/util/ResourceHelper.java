package com.samulle.plugin.update.util;

import android.content.res.Resources;

/**
 * 资源帮助类
 * 用来在不同的工程中加载资源
 * 避免了不同工程R文件路径不同的问题
 */
public class ResourceHelper {
    private String packageName;
    private Resources resources;
    /**
     * 更新内容的TextView ID
     */
    public static String TV_CONTENT = "tv_content";
    /**
     * 取消按钮ID
     */
    public static String BTN_CANCEL = "btn_cancel";
    /**
     * 下载布局文件
     */
    public static String DOWNLOAD_LAYOUT = "download_layout";
    /**
     * 确定按钮ID
     */
    public static String BTN_OK = "btn_ok";
    /**
     * 复选框ID
     */
    public static String CB_IGNORE = "cb_ignore";

    /**
     * 构造器
     * @param packageName   包名
     * @param resources     资源对象
     */
    public ResourceHelper(String packageName, Resources resources) {
        this.packageName = packageName;
        this.resources = resources;
    }

    /**
     * 获得ID资源
     * @param name  ID名称
     * @return
     */
    public int getId(String name) {
        return resources.getIdentifier(name, "id", packageName);
    }

    /**
     * 获得string资源
     * @param name  string资源名称
     * @return
     */
    public int getString(String name) {
        return resources.getIdentifier(name, "string", packageName);
    }

    /**
     * 获得layout资源
     * @param name  layout名称
     * @return
     */
    public int getLayout(String name) {
        return resources.getIdentifier(name, "layout", packageName);
    }
}
