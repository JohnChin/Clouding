package com.example.mypc.cloudstorage.bean;

/**
 * Created by My PC on 2018/2/28.
 */

public class XmlBean {
    private String xmlName; //xml名
    private String fileSize;//xml文件大小
    private String createTime;//xml创建时间

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }



    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }


}
