package com.example.mypc.cloudstorage.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.bean.UserBean;

import cn.bmob.v3.BmobUser;

public class PersonalProfileActivity extends AppCompatActivity {
    private TextView profileUserName;
    private UserBean currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("个人空间");

        currentUser= BmobUser.getCurrentUser(UserBean.class);
        initView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void initView(){
        profileUserName=(TextView)findViewById(R.id.profile_username);
        profileUserName.setText(currentUser.getUsername());
    }
}
