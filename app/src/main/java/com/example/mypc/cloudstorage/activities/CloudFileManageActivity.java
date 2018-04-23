package com.example.mypc.cloudstorage.activities;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.UIFitter.WrapContentLinearLayoutManager;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;
import com.example.mypc.cloudstorage.methods.ToolMethods;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

import static com.example.mypc.cloudstorage.InitService.oss;

public class CloudFileManageActivity extends AppCompatActivity {
    private RecyclerViewAdapter mRecylerViewAdapter;
    private RecyclerView mRecyclerView;
    private List<OSSObjectSummary> ossObjectSummaries;
    private Context context;
    private UserBean currentUser;
    private String userName;
    private GetInterDataMethods getInterDataMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file_manage);
        context = this;

        currentUser = BmobUser.getCurrentUser(UserBean.class);
        if (currentUser != null)
            userName = currentUser.getUsername() + "/";
        else {

        }

        View mainView = getWindow().getDecorView();

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("云端管理");

        getInterDataMethods = new GetInterDataMethods();

        ossObjectSummaries = new ArrayList<>();
        ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName));//获取云端存储文件数据

        mRecyclerView = (RecyclerView) findViewById(R.id.cloud_files);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mRecylerViewAdapter = new RecyclerViewAdapter<OSSObjectSummary>(context, R.layout.item_backupped_apks, ossObjectSummaries) {
            @Override
            protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                if (ossObjectSummary.getKey().contains(Config.TYPE_APK))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_android_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_SMS))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_mail_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_CONTACT))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_person_pin_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_IMG))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_photo_library_24dp);
                holder.setText(R.id.apk_name, ossObjectSummary.getKey());
            }
        };

        mRecylerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                LayoutInflater inflater = getLayoutInflater();
                View dialog = inflater.inflate(R.layout.dialog_inter_data, (ViewGroup) findViewById(R.id.linear_inter_data));
                AlertDialog cloudFileInterData = new AlertDialog.Builder(context).setView(dialog).create();
                TextView cotentFileKey = (TextView) dialog.findViewById(R.id.content_file_key);
                TextView cotentFileSize = (TextView) dialog.findViewById(R.id.content_file_size);
                Button deleteFile = (Button) dialog.findViewById(R.id.delete_cloud_file);

                cotentFileKey.setText(ossObjectSummaries.get(position).getKey());
                cotentFileSize.setText(ToolMethods.FormatFileSize(ossObjectSummaries.get(position).getSize()));

                deleteFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getInterDataMethods.deleteOssData(ossObjectSummaries.get(position).getKey(),mainView);
                        cloudFileInterData.dismiss();
                        RefreshData();
                    }
                });
                cloudFileInterData.setTitle("文件信息");
                cloudFileInterData.setView(dialog);
                cloudFileInterData.show();
            }
        });
        mRecyclerView.setAdapter(mRecylerViewAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void RefreshData(){
        ossObjectSummaries.clear();
        ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName));//获取云端存储文件数据
        mRecylerViewAdapter.notifyDataSetChanged();
    }
}
