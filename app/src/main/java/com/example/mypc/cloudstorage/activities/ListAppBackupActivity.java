package com.example.mypc.cloudstorage.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.BuildConfig;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.UIFitter.WrapContentLinearLayoutManager;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.Application;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.methods.ApkCatcher;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;
import com.example.mypc.cloudstorage.methods.ToolMethods;
import com.example.mypc.cloudstorage.methods.UpDownloadMethods;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class ListAppBackupActivity extends AppCompatActivity {
    public RecyclerView mRecyclerView,mRecyclerViewInter;
    public RecyclerViewAdapter mRecyclerViewAdapter,mRecyclerViewAdapterInter;
    public List<Application> myapks, selectedApks;
    public Context context;
    private View view;
    private UserBean currentUser;
    private String userName;
    private FloatingActionButton downloadFabutton,restoreFabutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_app_backup);

        currentUser= BmobUser.getCurrentUser(UserBean.class);
        if (currentUser!=null)
            userName=currentUser.getUsername()+"/";
        else {
            Intent intent=new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("已备份应用");
        context = this;
        view = getWindow().getDecorView();
        UpDownloadMethods upDownloadMethods = new UpDownloadMethods(view, context,currentUser);

        initView();

        downloadFabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GetInterDataMethods getInterDataMethods=new GetInterDataMethods();
                List<OSSObjectSummary> ossObjectSummaries = new ArrayList<OSSObjectSummary>();
                ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName+Config.TYPE_APK));//获取网站上apk数据
                LayoutInflater inflater = getLayoutInflater();
                View dialog = inflater.inflate(R.layout.dialog_app_internet_data, (ViewGroup) findViewById(R.id.liner_internet_data));
                AlertDialog alertDialogAppInterData =new AlertDialog.Builder(context).setView(dialog).create();
                mRecyclerViewInter = (RecyclerView)dialog.findViewById(R.id.internet_data);
                mRecyclerViewInter.setLayoutManager(new WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                mRecyclerViewAdapterInter=new RecyclerViewAdapter<OSSObjectSummary>(context,R.layout.item_backupped_apks,ossObjectSummaries) {
                    @Override
                    protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                        holder.setText(R.id.apk_name, ossObjectSummary.getKey());
                        holder.setImageResource(R.id.image_icon, R.drawable.ic_android_24dp);
                    }
                };
                mRecyclerViewAdapterInter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(RecyclerView parent, View view, int position) {
                        upDownloadMethods.downloadFile(ossObjectSummaries.get(position).getKey(),Config.TYPE_APK);
                        alertDialogAppInterData.cancel();
                    }
                });
                mRecyclerViewInter.setAdapter(mRecyclerViewAdapterInter);
                alertDialogAppInterData.setTitle("云端apk文件");
                alertDialogAppInterData.setMessage("\n点击下载");
                alertDialogAppInterData.setView(dialog);
                alertDialogAppInterData.show();
            }
        });

        restoreFabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i=0;i<myapks.size();i++){
                    installApp(i);
                }
            }
        });



        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        final ApkCatcher apkCatcher = new ApkCatcher(this);
        myapks = apkCatcher.allApks();//获取已备份的apk列表
        selectedApks = new ArrayList<>();

        mRecyclerViewAdapter = new RecyclerViewAdapter<Application>(this, R.layout.item_backupped_apks, myapks) {
            @Override
            protected void convert(ViewHolder holder, Application application) {
                holder.setText(R.id.apk_name, application.getAppName());
                holder.setImageResource(R.id.image_icon, R.drawable.ic_android_24dp);
            }
        };

        mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, final int position) {
               installApp(position);
            }
        });

        mRecyclerViewAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(RecyclerView parent, View view, int position) {
                AlertDialog.Builder backupSelecter = new AlertDialog.Builder(context);
                backupSelecter.setNegativeButton("上传", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String path = Config.APKS_PATH +myapks.get(position).getAppName() + Config.APK;
                        String name = myapks.get(position).getAppName();
                        upDownloadMethods.uploadData(path, name, Config.TYPE_APK);
                    }
                });
                backupSelecter.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteFile(position);
                    }
                });
                backupSelecter.setMessage("请选择您要进行的操作");
                backupSelecter.show();
            }
        });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

//    public void uploadApks(List<Application> apks){
//        for (int i=0;i<apks.size();i++){
//            String path = APKS_PATH + myapks.get(i).getAppName() + Config.APK;
//            uploadData(path,apks.get(i).getAppName());
//        }
//    }

    public void deleteFile(int position) {
        ToolMethods toolMethods = new ToolMethods();
        toolMethods.deleteFile(Config.APKS_PATH + myapks.get(position).getAppName() + Config.APK, context);
        myapks.remove(position);
        RefreshList();
    }

    public void RefreshList() {
        ApkCatcher apkCatcher=new ApkCatcher(context);
        myapks.clear();
        myapks.addAll(apkCatcher.allApks());
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void installApp(int position){
        File file = new File(Config.APKS_PATH + myapks.get(position).getAppName() + Config.APK);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void initView(){
        downloadFabutton=(FloatingActionButton)findViewById(R.id.fab_download);
        mRecyclerView = (RecyclerView) findViewById(R.id.list_backupped_apks);
        restoreFabutton=(FloatingActionButton)findViewById(R.id.fab_install);
    }
}
