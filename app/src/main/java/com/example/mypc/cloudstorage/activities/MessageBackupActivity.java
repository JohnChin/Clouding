package com.example.mypc.cloudstorage.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.UIFitter.WrapContentLinearLayoutManager;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.asyctask.SmsTask;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.bean.XmlBean;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;
import com.example.mypc.cloudstorage.methods.SmsBackupMethods;
import com.example.mypc.cloudstorage.methods.ToolMethods;
import com.example.mypc.cloudstorage.methods.UpDownloadMethods;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class MessageBackupActivity extends AppCompatActivity {
    private Button dialogUploadButton, dialogDeleteButton, dialogRestoreButton, xmlInfoDeleteButton;
    private Context context;
    private TextView xmlFileName, xmlFileSize, xmlFileTime;
    public RecyclerView mRecyclerView, mRecyclerViewInter;
    public RecyclerViewAdapter mRecyclerViewAdapter, mRecyclerViewAdapterInter;
    private List<XmlBean> allSms;
    private File baseFile;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH_mm");
    private SmsBackupMethods smsBackupMethods;
    private FloatingActionButton xmlFab, downloadFab;
    private UserBean currentUser;
    private String userName;
    private String defaultSmsApp;
    private String myPackageName;
    private int flag = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_backup);
        context = this;

        myPackageName = getPackageName();

        currentUser = BmobUser.getCurrentUser(UserBean.class);
        if (currentUser != null)
            userName = currentUser.getUsername() + "/";
        else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);//获取默认短信应用
        }
        baseFile = new File(Config.SMS_FILE_PATH);//短信备份文件本地存储地址


        View view = getWindow().getDecorView();
        UpDownloadMethods upDownloadMethods = new UpDownloadMethods(view, context,currentUser);


        smsBackupMethods = new SmsBackupMethods(context);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("短信备份");

        mRecyclerView = (RecyclerView) findViewById(R.id.xml_info);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        allSms = smsBackupMethods.allSmsXmls();

        mRecyclerViewAdapter = new RecyclerViewAdapter<XmlBean>(this, R.layout.item_backupped_apks, allSms) {
            @Override
            protected void convert(ViewHolder holder, XmlBean xmlBean) {
                holder.setText(R.id.apk_name, xmlBean.getXmlName());
                holder.setImageResource(R.id.image_icon, R.drawable.ic_mail_24dp);
            }
        };
        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                LayoutInflater inflater = getLayoutInflater();
                View smsLayout = inflater.inflate(R.layout.dialog_xml_infos, (ViewGroup) findViewById(R.id.xml_infos_linear));
                AlertDialog smsInfosDialog = new AlertDialog.Builder(context).setView(smsLayout).create();

                xmlFileName = (TextView) smsLayout.findViewById(R.id.xml_file_name);
                xmlFileSize = (TextView) smsLayout.findViewById(R.id.xml_file_size);
                xmlFileTime = (TextView) smsLayout.findViewById(R.id.xml_file_create_time);
                xmlFileName.setText(allSms.get(position).getXmlName());
                xmlFileSize.setText(allSms.get(position).getFileSize());
                xmlFileTime.setText(allSms.get(position).getCreateTime());

                xmlInfoDeleteButton = (Button) smsLayout.findViewById(R.id.delete_xml);
                xmlInfoDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteFile(position);
                        smsInfosDialog.dismiss();
                    }
                });

                smsInfosDialog.setTitle("备份文件信息");
                smsInfosDialog.show();
            }
        });
        mRecyclerViewAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(RecyclerView parent, View view, int position) {
                LayoutInflater inflater = getLayoutInflater();
                View dialog = inflater.inflate(R.layout.view_dialog_slecters, (ViewGroup) findViewById(R.id.linearLayout_buttons));
                AlertDialog buttonsDialog = new AlertDialog.Builder(context).setView(dialog).create();
                dialogUploadButton = (Button) dialog.findViewById(R.id.dialog_upload);
                dialogUploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = Config.SMS_FILE_PATH + allSms.get(position).getXmlName() + Config.XML;
                        String name = allSms.get(position).getXmlName();
                        upDownloadMethods.uploadData(path, name, Config.TYPE_SMS);
                        buttonsDialog.dismiss();//上传短信文件
                    }
                });
                dialogRestoreButton = (Button) dialog.findViewById(R.id.dialog_restore);
                dialogRestoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = new File(Config.SMS_FILE_PATH + allSms.get(position).getXmlName() + ".xml");
                        FileInputStream fin = null;
                        try {
                            fin = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        smsBackupMethods.restoreSms(fin);
                        buttonsDialog.dismiss();
                    }
                });
                dialogDeleteButton = (Button) dialog.findViewById(R.id.dialog_delete);
                dialogDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteFile(position);
                        buttonsDialog.dismiss();
                    }
                });
                buttonsDialog.setTitle("短信文件");
                buttonsDialog.setMessage(allSms.get(position).getXmlName());
                buttonsDialog.show();
            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        xmlFab = (FloatingActionButton) findViewById(R.id.fab_xml_backup_sms);
        xmlFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ToolMethods toolMethods = new ToolMethods();
                    toolMethods.createDocument(Config.SMS_FILE_PATH);
                    AsyncTask<Void, Void, String> smsTask = new SmsTask(context, new File(baseFile, "sms" + format.format(new Date()) + ".xml"));
                    smsTask.execute();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        downloadFab = (FloatingActionButton) findViewById(R.id.fab_download_sms);
        downloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetInterDataMethods getInterDataMethods = new GetInterDataMethods();
                List<OSSObjectSummary> ossObjectSummaries = new ArrayList<OSSObjectSummary>();
                ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName + Config.TYPE_SMS));//获取网站上apk数据
                LayoutInflater inflater = getLayoutInflater();
                View dialog = inflater.inflate(R.layout.dialog_app_internet_data, (ViewGroup) findViewById(R.id.liner_internet_data));
                AlertDialog alertDialogAppInterData = new AlertDialog.Builder(context).setView(dialog).create();
                mRecyclerViewInter = (RecyclerView) dialog.findViewById(R.id.internet_data);
                mRecyclerViewInter.setLayoutManager(new WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                mRecyclerViewAdapterInter = new RecyclerViewAdapter<OSSObjectSummary>(context, R.layout.item_backupped_apks, ossObjectSummaries) {
                    @Override
                    protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                        holder.setText(R.id.apk_name, ossObjectSummary.getKey());
                        holder.setImageResource(R.id.image_icon, R.drawable.ic_mail_24dp);
                    }
                };
                mRecyclerViewAdapterInter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View view, int position) {
                        upDownloadMethods.downloadFile(ossObjectSummaries.get(position).getKey(),Config.TYPE_SMS);
                        alertDialogAppInterData.dismiss();
                    }
                });
                mRecyclerViewInter.setAdapter(mRecyclerViewAdapterInter);
                alertDialogAppInterData.setTitle("云端apk文件");
                alertDialogAppInterData.setView(dialog);
                alertDialogAppInterData.show();
            }
        });
//        uploadButton.findViewById(R.id.upload_sms_button);
    }

    public void RefreshList() {
        SmsBackupMethods smsBackupMethods = new SmsBackupMethods(context);
        allSms.clear();
        allSms.addAll(smsBackupMethods.allSmsXmls());
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void deleteFile(int position) {
        ToolMethods toolMethods = new ToolMethods();
        toolMethods.deleteFile(Config.SMS_FILE_PATH + allSms.get(position).getXmlName() + Config.XML, context);
        allSms.remove(position);
        RefreshList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
                if (flag == 1) {
                    finish();
                    Toast.makeText(context, "必须将本程序设置为默认信息程序", Toast.LENGTH_LONG).show();
                } else
                    applySms();
            } else {

            }
        }

    }

    public void applySms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!Telephony.Sms.getDefaultSmsPackage(context).equals(myPackageName)) {
                flag = 1;
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
                startActivity(intent);
            }
        } else {

        }
    }
}
