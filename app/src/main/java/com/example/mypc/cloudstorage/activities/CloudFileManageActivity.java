package com.example.mypc.cloudstorage.activities;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.UIFitter.WrapContentLinearLayoutManager;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class CloudFileManageActivity extends AppCompatActivity {
    private RecyclerViewAdapter mRecylerViewAdapter;
    private RecyclerView mRecyclerView;
    private List<OSSObjectSummary> ossObjectSummaries;
    private Context context;
    private UserBean currentUser;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file_manage);
        context=this;

        currentUser= BmobUser.getCurrentUser(UserBean.class);
        if (currentUser!=null)
            userName=currentUser.getUsername()+"/";
        else {

        }

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("云端管理");

        GetInterDataMethods getInterDataMethods=new GetInterDataMethods();
        ossObjectSummaries=new ArrayList<>();
        ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName));//获取云端存储文件数据

        mRecyclerView=(RecyclerView)findViewById(R.id.cloud_files);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mRecylerViewAdapter=new RecyclerViewAdapter<OSSObjectSummary>(context,R.layout.item_backupped_apks,ossObjectSummaries) {
            @Override
            protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                if (ossObjectSummary.getKey().contains(Config.TYPE_APK))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_android_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_SMS))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_mail_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_CONTACT))
                    holder.setImageResource(R.id.image_icon, R.drawable.ic_person_pin_24dp);
                if (ossObjectSummary.getKey().contains(Config.TYPE_IMG))
                    holder.setImageResource(R.id.image_icon,R.drawable.ic_photo_library_24dp);
                holder.setText(R.id.apk_name, ossObjectSummary.getKey());
            }
        };
        mRecyclerView.setAdapter(mRecylerViewAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
