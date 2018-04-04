package com.example.mypc.cloudstorage.bean;

/**
 * Created by My PC on 2018/2/27.
 */

public class SmsInfo {
    //电话号码
    private String address;
    //日期
    private String date;
    //短信类型
    private String type;
    //短信内容
    private String body;
    //短信id
    private int id;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
