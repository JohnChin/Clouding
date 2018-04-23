package com.example.mypc.cloudstorage.methods;

import android.content.Context;

import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.Application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by My PC on 2018/2/26.
 */

public class ApkCatcher {
    private File file;
    private Context context;
    private ToolMethods toolMethods;
    List<Application> lists = new ArrayList<Application>();

    public ApkCatcher(Context context){
        this.context=context;
    }

    public void copyFile(Application application) {
        String sourceDir=application.getSourceDir();
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(sourceDir);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(sourceDir); //读入原文件
                toolMethods=new ToolMethods();
                toolMethods.createDocument(Config.APKS_PATH);

                FileOutputStream fs = new FileOutputStream(Config.APKS_PATH+application.getAppName()+Config.APK);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Application> allApks() {
        File path = new File(Config.APKS_PATH);
        File[] files = path.listFiles();
        getFiles(files);
        return lists;
    }

    public void getFiles(File[] files){
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getFiles(file.listFiles());
                } else {
                    ToolMethods toolMethods=new ToolMethods();
                    String fileName = file.getName();
                    String fileSize=toolMethods.FormatFileSize(file.length());
                    String time = new SimpleDateFormat("yyyy-MM-dd")
                            .format(new Date(file.lastModified()));
                    if (fileName.endsWith(Config.APK)) {
                        Application application=new Application();
                        String s = fileName.substring(0, fileName.lastIndexOf(".")).toString();
                        application.setAppName(s);
                        application.setApkSize(fileSize);
                        application.setCreateTime(time);
                        lists.add(application);
                    }
                }
            }
        }
    }
}
