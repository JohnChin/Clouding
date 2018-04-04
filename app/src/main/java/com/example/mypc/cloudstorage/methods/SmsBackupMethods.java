package com.example.mypc.cloudstorage.methods;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.SmsInfo;
import com.example.mypc.cloudstorage.bean.XmlBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by My PC on 2018/2/27.
 */

public class SmsBackupMethods {
    private Context context;
    private List<XmlBean> lists = new ArrayList<>();


    public SmsBackupMethods(Context context) {
        this.context = context;
    }

    public List<XmlBean> allSmsXmls() {
        File path = new File(Config.SMS_FILE_PATH);
        File[] files = path.listFiles();
        getFiles(files);
        return lists;
    }

    public void getFiles(File[] files) {
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
                    if (fileName.endsWith(Config.XML)) {
                        XmlBean xmlBean = new XmlBean();
                        String s = fileName.substring(0, fileName.lastIndexOf(".")).toString();
                        xmlBean.setXmlName(s);
                        xmlBean.setFileSize(fileSize);
                        xmlBean.setCreateTime(time);
                        lists.add(xmlBean);
                    }
                }
            }
        }
    }

    public List<SmsInfo> getSmsInfo(InputStream inStream) {//获取短信内容保存在List中
        List<SmsInfo> smsInfos = new ArrayList<>();
        SmsInfo smsInfo = null;
        XmlPullParser pullParser = Xml.newPullParser();
        try {
            pullParser.setInput(inStream, String.valueOf(Xml.Encoding.UTF_8));
            int event = pullParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("smss".equalsIgnoreCase(pullParser.getName())) {

                        }
                        if ("sms".equalsIgnoreCase(pullParser.getName())){
                            int id = 1;
                            smsInfo = new SmsInfo();
                            smsInfo.setId(id);
                        }
                        if (smsInfo != null) {
                            if ("address".equals(pullParser.getName())) {
                                smsInfo.setAddress(pullParser.nextText());
                            }
                            if ("date".equals(pullParser.getName())) {
                                smsInfo.setDate(pullParser.nextText());
                            }
                            if ("type".equals(pullParser.getName())) {
                                smsInfo.setType(pullParser.nextText());
                            }
                            if ("body".equals(pullParser.getName())) {
                                smsInfo.setBody(pullParser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("sms".equals(pullParser.getName())) {
                            smsInfos.add(smsInfo);
                            smsInfo = null;
                        }
                        break;
                    default:
                        break;
                }
                event = pullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return smsInfos;
    }


    public void restoreSms(InputStream inputStream) {
        //1 删除全部的短信
        //2 把xml里面的数据插入到短信的数据库
        //2.1 先解析xml文件
        //2.2 插入数据
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在删除原来的短信");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(){
            public void run() {
                try {
                    Uri uri = Uri.parse("content://sms/");
                    context.getContentResolver().delete(uri, null, null);
                    progressDialog.setTitle("正在还原短信");
                    List<SmsInfo> smsInfos = getSmsInfo(inputStream);
                    progressDialog.setMax(smsInfos.size());
                    for(SmsInfo smsInfo:smsInfos){
                        ContentValues values = new ContentValues();
                        values.clear();
                        values.put("address", smsInfo.getAddress());
                        values.put("date", smsInfo.getDate());
                        values.put("type", smsInfo.getType());
                        values.put("body", smsInfo.getBody());
                        context.getContentResolver().insert(uri, values);
                        progressDialog.incrementProgressBy(1);//每次进度条刻度值加1
                        Log.w("ss",String.valueOf(values.size()));
                    }
                    progressDialog.dismiss();
                    Looper.prepare();
                    Toast.makeText(context, "短信还原成功", Toast.LENGTH_LONG).show();
                    Looper.loop();

                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Looper.prepare();
                    Toast.makeText(context, "短信还原失败", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }

            };
        }.start();
    }
}
