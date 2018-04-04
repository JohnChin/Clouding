package com.example.mypc.cloudstorage.bean;

/**
 * Created by My PC on 2018/3/1.
 */
import java.util.List;

public class ContactInfo {

    private int id;
    private String name;
    private List<String> phoneNum;

    public ContactInfo() {
        super();
    }

    public ContactInfo(int id, String name, List<String> phoneNum) {
        super();
        this.id = id;//id
        this.name = name;//联系人姓名
        this.phoneNum = phoneNum;//联系人电话（可能存在多个电话，用List容器）
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(List<String> phoneNum) {
        this.phoneNum = phoneNum;
    }

}
