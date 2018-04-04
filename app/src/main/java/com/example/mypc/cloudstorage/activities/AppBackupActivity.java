package com.example.mypc.cloudstorage.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.bean.Application;
import com.example.mypc.cloudstorage.methods.ApkCatcher;
import com.example.mypc.cloudstorage.methods.AppInfoProvider;

import java.util.List;


public class AppBackupActivity extends AppCompatActivity {
    public RecyclerView mRecyclerView;
    public RecyclerViewAdapter mRecyclerViewAdapter;
    public List<Application> myapplication;
    public AppInfoProvider appInfoProvider;
    public ApkCatcher apkCatcher;
    public ProgressDialog dialog, backDialog;
    public Context context;
    private Handler handler = null;
    private TextView dialogAppName, dialogAppSize, dialogAppPack, sourceDir;
    private ImageView dialogIcon;
    private RelativeLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("App备份");

        context = this;//获取上下文
        handler = new Handler();//读取手机应用，成功后发消息刷新列表

        setContentView(R.layout.activity_app_backup);
        appInfoProvider = new AppInfoProvider(this);
        apkCatcher = new ApkCatcher(this);
        initView();

    }

    public void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.app_info);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        container = (RelativeLayout) findViewById(R.id.container_app_backup);

        dialog = new ProgressDialog(AppBackupActivity.this);
        dialog.setCancelable(false);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        dialog.setTitle("请稍后");
        dialog.setMessage("正在读取手机上的应用");
        dialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    myapplication = appInfoProvider.getAllApps(context);//读取系统应用列表此方法为耗时操作，需要dialog进行提示
                } catch (Exception e) {
                }
                // 在进度条走完时删除Dialog
                dialog.dismiss();
                handler.post(runnableUi);
            }
        }).start();
    }

    Runnable runnableUi = new Runnable() {//应用列表读入
        @Override
        public void run() {
            mRecyclerViewAdapter = new RecyclerViewAdapter<Application>(getApplicationContext(),
                    R.layout.item_app_info, myapplication) {
                @Override
                protected void convert(ViewHolder holder, Application application) {
                    holder.setImageBitmap(R.id.app_info_icon, drawableToBitmap(application.getIcon()));
                    holder.setText(R.id.app_info_name, application.getAppName());
                }
            };

            mRecyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(RecyclerView parent, View view, final int position) {
                    final AlertDialog.Builder appInfoDialog = new AlertDialog.Builder(AppBackupActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialog = inflater.inflate(R.layout.item_of_app_info, (ViewGroup) findViewById(R.id.item_of_app_info));
                    dialogAppName = (TextView) dialog.findViewById(R.id.app_info_check_content_of_app_name);
                    dialogAppSize = (TextView) dialog.findViewById(R.id.app_info_check_content_of_app_size);
                    dialogAppPack = (TextView) dialog.findViewById(R.id.app_info_check_content_of_app_packgename);
                    dialogIcon = (ImageView) dialog.findViewById(R.id.app_info_check_title_of_app_icon);
                    sourceDir = (TextView) dialog.findViewById(R.id.app_info_check_content_of_app_sourceDir);

                    dialogAppName.setText(myapplication.get(position).getAppName());

                    dialogAppSize.setText(formateFileSize(myapplication.get(position).getCodesize()));

                    dialogAppPack.setText(myapplication.get(position).getPackageName());
                    dialogIcon.setImageBitmap(drawableToBitmap(myapplication.get(position).getIcon()));
                    sourceDir.setText(myapplication.get(position).getSourceDir());

                    appInfoDialog.setPositiveButton("备份", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            apkCatcher.copyFile(myapplication.get(position));

                            Snackbar.make(container, "保存成功  " + myapplication.get(position).getAppName() + ".apk", Snackbar.LENGTH_LONG).show();
                            new Handler().postDelayed(new Runnable() {//防止此Activity跳转后没有保存成功的提示，延时1秒后执行
                                public void run() {
                                    Intent intent = new Intent(AppBackupActivity.this, ListAppBackupActivity.class);
                                    startActivity(intent);
                                }
                            }, 1000);
                        }
                    });
                    appInfoDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    appInfoDialog.setTitle("应用信息");
                    appInfoDialog.setView(dialog);
                    appInfoDialog.show();
                }
            });
            mRecyclerViewAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(RecyclerView parent, View view, int position) {
                }
            });
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_backup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_check_list_of_apk) {
            Intent intent = new Intent(AppBackupActivity.this, ListAppBackupActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


    private String formateFileSize(long size) {//long类型转FileSize
        return Formatter.formatFileSize(context, size);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {//drawable转bitmap
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null && !this.isFinishing()) {
            dialog.dismiss();
        }
    }
}
