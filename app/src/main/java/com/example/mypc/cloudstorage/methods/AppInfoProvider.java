package com.example.mypc.cloudstorage.methods;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;

import com.example.mypc.cloudstorage.bean.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by My PC on 2017/11/26.
 */

public class AppInfoProvider {
    private PackageManager packageManager;
    //获取一个包管理器
    public AppInfoProvider(Context context){
        packageManager = context.getPackageManager();
    }
    /**
     *获取系统中所有应用信息，
     *并将应用软件信息保存到list列表中。
     **/
    public List<Application> getAllApps(Context context){
        List<Application> list = new ArrayList<Application>();
        Application myAppInfo;
        //获取到所有安装了的应用程序的信息，包括那些卸载了的，但没有清除数据的应用程序
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for(PackageInfo info:packageInfos){
            myAppInfo = new Application();
            //拿到包名
            String packageName = info.packageName;
            //拿到应用程序的信息
            ApplicationInfo appInfo = info.applicationInfo;
            //拿到应用程序的图标
            Drawable icon = appInfo.loadIcon(packageManager);
            //拿到应用程序的大小
            long codesize = getPkgSize(context,packageName,myAppInfo);
            //拿到应用程序的程序名
            String appName = appInfo.loadLabel(packageManager).toString();

            String sourceDir=appInfo.sourceDir;
            myAppInfo.setPackageName(packageName);
            myAppInfo.setAppName(appName);
            myAppInfo.setIcon(icon);
            myAppInfo.setCodesize(codesize);
            myAppInfo.setSourceDir(sourceDir);

            if(filterApp(appInfo)){
                myAppInfo.setSystemApp(false);
                list.add(myAppInfo);
            }else{
                myAppInfo.setSystemApp(true);
            }
        }
        return list;
    }
    /**
     *判断某一个应用程序是不是系统的应用程序，
     *如果是返回true，否则返回false。
     */

    public boolean filterApp(ApplicationInfo info){
        //有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，它还是系统应用，这个就是判断这种情况的
        if((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
            return true;
        }else if((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0){//判断是不是系统应用
            return true;
        }
        return false;
    }

    public static Long getPkgSize(final Context context, String pkgName, final Application appInfo) {
        // getPackageSizeInfo是PackageManager中的一个private方法，所以需要通过反射的机制来调用
        Method method;
        try {
            method = PackageManager.class.getMethod("getPackageSizeInfo",
                    new Class[]{String.class, IPackageStatsObserver.class});
            // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
            method.invoke(context.getPackageManager(), pkgName,
                    new IPackageStatsObserver.Stub() {
                        @Override
                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                            if (succeeded && pStats != null) {
                                synchronized (Application.class) {
                                    appInfo.setCodesize(pStats.codeSize+pStats.dataSize+pStats.cacheSize); //应用大小
                                }
                            }
                        }
                    });
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return appInfo.getCodesize();
    }
}
