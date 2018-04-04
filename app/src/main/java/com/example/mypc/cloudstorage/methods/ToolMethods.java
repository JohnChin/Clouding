package com.example.mypc.cloudstorage.methods;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Created by My PC on 2018/2/27.
 */

public class ToolMethods {

    public void createDocument(String path){
        File file = new File(path);//判断文件夹是否存在，如果不存在就创建，否则不创建
        if (!file.exists()) {
            //通过file的mkdirs()方法创建目录中包含却不存在的文件夹
            file.mkdirs();
        }
    }

    public boolean deleteFile(String fileName, Context context) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Toast.makeText(context,"删除"+fileName+"成功！",Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(context,"删除"+fileName+"失败！",Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Toast.makeText(context,"删除失败"+fileName+"不存在",Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }


}
