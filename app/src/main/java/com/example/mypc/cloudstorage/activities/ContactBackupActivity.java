package com.example.mypc.cloudstorage.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.UIFitter.WrapContentLinearLayoutManager;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.asyctask.ContactTask;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.bean.XmlBean;
import com.example.mypc.cloudstorage.methods.ContactBackupMethods;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;
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

public class ContactBackupActivity extends AppCompatActivity {
    public Button dialogUploadButton, dialogRestoreButton,dialogdeleteButton,xmlInfoDeleteButton;
    public RecyclerView mRecyclerView,mRecyclerViewInter;
    public RecyclerViewAdapter mRecyclerViewAdapter,mRecyclerViewAdapterInter;
    private TextView xmlFileName,xmlFileSize,xmlFileTime;
    private List<XmlBean> allContacts;//所有的联系人备份列表
    private Context context;
    private FloatingActionButton xmlFab,downloadFab;
    private  AsyncTask<Void, Void, String> contactTask;

    private File baseFile;
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH_mm");

    private UserBean currentUser;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_backup);

        currentUser= BmobUser.getCurrentUser(UserBean.class);
        if (currentUser!=null)
            userName=currentUser.getUsername()+"/";
        else {
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }

        baseFile = new File(Config.CONTACT_FILE_PATH);

        context=this;
        View view = getWindow().getDecorView();

        UpDownloadMethods upDownloadMethods=new UpDownloadMethods(view,context,currentUser);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("联系人备份");

        initView();


        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        ContactBackupMethods contactBackupMethods=new ContactBackupMethods(context);

        allContacts=contactBackupMethods.allContactXmls();

        mRecyclerViewAdapter = new RecyclerViewAdapter<XmlBean>(this, R.layout.item_backupped_apks, allContacts) {
            @Override
            protected void convert(ViewHolder holder, XmlBean xmlBean) {
                holder.setText(R.id.apk_name, xmlBean.getXmlName());
                holder.setImageResource(R.id.image_icon, R.drawable.ic_person_pin_24dp);
            }
        };
        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {

                LayoutInflater inflater = getLayoutInflater();
                View smsLayout=inflater.inflate(R.layout.dialog_xml_infos,(ViewGroup)findViewById(R.id.xml_infos_linear));
                AlertDialog  smsInfosDialog =new AlertDialog.Builder(context).setView(smsLayout).create();
                xmlFileName=(TextView)smsLayout.findViewById(R.id.xml_file_name);
                xmlFileSize=(TextView)smsLayout.findViewById(R.id.xml_file_size);
                xmlFileTime=(TextView)smsLayout.findViewById(R.id.xml_file_create_time);
                xmlFileName.setText(allContacts.get(position).getXmlName());
                xmlFileSize.setText(allContacts.get(position).getFileSize());
                xmlFileTime.setText(allContacts.get(position).getCreateTime());

                xmlInfoDeleteButton=(Button)smsLayout.findViewById(R.id.delete_xml);
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

                dialogUploadButton=(Button)dialog.findViewById(R.id.dialog_upload);
                dialogUploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = Config.CONTACT_FILE_PATH + allContacts.get(position).getXmlName() + Config.XML;
                        String name = allContacts.get(position).getXmlName();
                        upDownloadMethods.uploadData(path, name, Config.TYPE_CONTACT);
                        buttonsDialog.dismiss();
                    }
                });
                dialogRestoreButton =(Button)dialog.findViewById(R.id.dialog_restore);
                dialogRestoreButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File file = new File(Config.CONTACT_FILE_PATH+allContacts.get(position).getXmlName()+Config.XML);
                        FileInputStream fin = null;
                        try {
                            fin = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
//                        smsBackupMethods.restoreSms(fin);
                        buttonsDialog.dismiss();
                        contactBackupMethods.RestoreContact(fin);
                    }
                });
                dialogdeleteButton=(Button)dialog.findViewById(R.id.dialog_delete);
                dialogdeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteFile(position);
                        buttonsDialog.dismiss();
                    }
                });
                buttonsDialog.setTitle("联系人文件");
                buttonsDialog.setMessage(allContacts.get(position).getXmlName());
                buttonsDialog.show();
            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);


        xmlFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolMethods toolMethods=new ToolMethods();
                toolMethods.createDocument(Config.CONTACT_FILE_PATH);
                try {
                    contactTask = new ContactTask(context, new File(baseFile, "contact" + format.format(new Date()) + ".xml"));
                    contactTask.execute();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        downloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetInterDataMethods getInterDataMethods=new GetInterDataMethods();
                List<OSSObjectSummary> ossObjectSummaries = new ArrayList<OSSObjectSummary>();
                ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName+Config.TYPE_CONTACT));//获取网站上联系人数据
                LayoutInflater inflater = getLayoutInflater();
                View dialog = inflater.inflate(R.layout.dialog_app_internet_data, (ViewGroup) findViewById(R.id.liner_internet_data));
                AlertDialog alertDialogContactInterData =new AlertDialog.Builder(context).setView(dialog).create();
                mRecyclerViewInter = (RecyclerView)dialog.findViewById(R.id.internet_data);
                mRecyclerViewInter.setLayoutManager(new WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                mRecyclerViewAdapterInter=new RecyclerViewAdapter<OSSObjectSummary>(context,R.layout.item_backupped_apks,ossObjectSummaries) {
                    @Override
                    protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                        holder.setText(R.id.apk_name, ossObjectSummary.getKey());
                        holder.setImageResource(R.id.image_icon, R.drawable.ic_person_pin_24dp);
                    }
                };
                mRecyclerViewAdapterInter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View view, int position) {
                        upDownloadMethods.downloadFile(ossObjectSummaries.get(position).getKey(),Config.TYPE_CONTACT);
                        alertDialogContactInterData.dismiss();
                    }
                });
                mRecyclerViewInter.setAdapter(mRecyclerViewAdapterInter);
                alertDialogContactInterData.setTitle("云端联系人文件");
                alertDialogContactInterData.setMessage("\n点击下载");
                alertDialogContactInterData.setView(dialog);
                alertDialogContactInterData.show();
            }
        });
    }

    public void RefreshList(){
        ContactBackupMethods contactBackupMethods=new ContactBackupMethods(context);
        allContacts.clear();
        allContacts.addAll(contactBackupMethods.allContactXmls());
        mRecyclerViewAdapter.notifyDataSetChanged();//刷新列表
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
    public void deleteFile(int position){
        ToolMethods toolMethods =new ToolMethods();
        toolMethods.deleteFile(Config.CONTACT_FILE_PATH +allContacts.get(position).getXmlName()+ Config.XML,context);
        allContacts.remove(position);
        RefreshList();
    }

    public void initView(){
        downloadFab=(FloatingActionButton)findViewById(R.id.fab_download_contact);
        mRecyclerView = (RecyclerView) findViewById(R.id.xml_info_contact);
        xmlFab=(FloatingActionButton)findViewById(R.id.fab_xml_backup_contact);
    }
}
