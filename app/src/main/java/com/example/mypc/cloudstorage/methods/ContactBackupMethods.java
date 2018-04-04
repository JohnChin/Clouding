package com.example.mypc.cloudstorage.methods;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Xml;
import android.widget.Toast;

import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.ContactInfo;
import com.example.mypc.cloudstorage.bean.XmlBean;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentUris;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Data;
import android.widget.Toast;
/**
 * Created by My PC on 2018/3/9.
 */

public class ContactBackupMethods {
    private Context context;
    private List<XmlBean> lists = new ArrayList<>();
    private Handler handler = null;
    public ContactBackupMethods(Context context) {
        this.context = context;
    }

    public List<XmlBean> allContactXmls() {
        File path = new File(Config.CONTACT_FILE_PATH);
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
                    ToolMethods toolMethods =new ToolMethods();
                    String fileName = file.getName();
                    String fileSize=toolMethods.FormatFileSize(file.length());
                    String time = new SimpleDateFormat("yyyy-MM-dd")
                            .format(new Date(file.lastModified()));
                    if (fileName.endsWith(".xml")) {
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

    public List<ContactInfo> getContactInfo(InputStream inStream) {//获取短信内容保存在List中
        List<ContactInfo> contactInfos = new ArrayList<>();
        ContactInfo contactInfo = null;
        XmlPullParser pullParser = Xml.newPullParser();
        List<String> phones=null;

        try {
            pullParser.setInput(inStream, String.valueOf(Xml.Encoding.UTF_8));
            int event = pullParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("contacts".equalsIgnoreCase(pullParser.getName())){

                        }
                        if ("contact".equalsIgnoreCase(pullParser.getName())) {
                            int id = 1;
                            contactInfo = new ContactInfo();
                            contactInfo.setId(id);
                            phones=new ArrayList<>();
                        }
                        if (contactInfo != null) {
                            if ("name".equals(pullParser.getName())) {
                                contactInfo.setName(pullParser.nextText());
                            }
                            if ("phone".equals(pullParser.getName())) {
                               phones.add(pullParser.nextText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("contact".equals(pullParser.getName())) {
                            contactInfo.setPhoneNum(phones);
                            contactInfos.add(contactInfo);
                            phones=null;//每当完成一个联系人备份，清空电话表
                            contactInfo = null;//清空联系人表
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
        return contactInfos;
    }

    public void RestoreContact(InputStream inputStream) {
        // 创建一个空的ContentValues
        List<ContactInfo> contactInfos=getContactInfo(inputStream);
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("正在恢复");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setMax(contactInfos.size());
        progressDialog.show();

        Runnable runnableToast = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "联系人数据恢复成功", Toast.LENGTH_SHORT).show();
            }
        };

        handler=new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ContactInfo contactInfo:contactInfos){
                        ContentValues values = new ContentValues();
                        Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
                        long rawContactId = ContentUris.parseId(rawContactUri);
                        // 下面的操作会根据RawContacts表中已有的rawContactId使用情况自动生成新联系人的rawContactId
                        // 向data表插入姓名数据
                        values.clear();
                        values.put(Data.RAW_CONTACT_ID, rawContactId);
                        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                        values.put(StructuredName.GIVEN_NAME, contactInfo.getName());
                        context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
                        // 向data表插入电话数据
                        for (int i=0;i<contactInfo.getPhoneNum().size();i++){
                            values.clear();
                            values.put(Data.RAW_CONTACT_ID, rawContactId);
                            values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                            values.put(Phone.NUMBER, contactInfo.getPhoneNum().get(i));
                            values.put(Phone.TYPE, Phone.TYPE_MOBILE);
                            context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);
                        }
                        progressDialog.incrementProgressBy(1);
                    }
                } catch (Exception e) {
                }
                // 在进度条走完时删除Dialog
                progressDialog.dismiss();
                handler.post(runnableToast);
            }
        }).start();
    }
}
