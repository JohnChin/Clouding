package com.example.mypc.cloudstorage.bean;

import android.graphics.Bitmap;

/**
 * Created by My PC on 2018/3/1.
 */

public class Picture {
    private String picName;//图片名
    private String picPath;//图片路径
    private String key;//图片云端key
    private Bitmap source;//图片内容

    public Bitmap getSource() {
        return source;
    }

    public void setSource(Bitmap source) {
        this.source = source;
    }



    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }
    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

}
