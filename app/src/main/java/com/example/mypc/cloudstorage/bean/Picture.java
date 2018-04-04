package com.example.mypc.cloudstorage.bean;

import android.graphics.Bitmap;

/**
 * Created by My PC on 2018/3/1.
 */

public class Picture {
    private String picName;

    private String picPath;
    private String key;
    private Bitmap source;
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
