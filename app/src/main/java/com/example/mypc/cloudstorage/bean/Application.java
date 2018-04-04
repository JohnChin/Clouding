package com.example.mypc.cloudstorage.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by My PC on 2017/11/26.
 */

public class Application {
    private Drawable icon;//
    private String appName;//
    private String packageName;//
    private boolean isSystemApp;//
    private long codesize;//
    private String sourceDir;//
    private String createTime;
    private String apkSize;


    public String getApkSize() {
        return apkSize;
    }

    public void setApkSize(String apkSize) {
        this.apkSize = apkSize;
    }
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public void setSystemApp(boolean systemApp) {
        isSystemApp = systemApp;
    }

    public long getCodesize() {
        return codesize;
    }

    public void setCodesize(long codesize) {
        this.codesize = codesize;
    }

}
