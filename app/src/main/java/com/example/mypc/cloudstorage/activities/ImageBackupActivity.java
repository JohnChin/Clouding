package com.example.mypc.cloudstorage.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.R;
import com.example.mypc.cloudstorage.UIFitter.RecyclerViewAdapter;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.Picture;
import com.example.mypc.cloudstorage.bean.UserBean;
import com.example.mypc.cloudstorage.methods.GetInterDataMethods;
import com.example.mypc.cloudstorage.methods.LoadImageMethods;
import com.example.mypc.cloudstorage.methods.UpDownloadMethods;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class ImageBackupActivity extends AppCompatActivity {
    private static final int IMAGE = 1;
    private UserBean currentUser;
    private String imagePath = null;

    public RecyclerView mRecyclerViewInter;
    public RecyclerViewAdapter mRecyclerViewAdapterInter;
    private Context context;
    private FloatingActionButton fabUploadImage, fabDownLoadImage, fabAddImage;
    private LoadImageMethods loadImageMethods;

    private String userName;
    private List<Picture> pictures = new ArrayList<>();

    private UpDownloadMethods upDownloadMethods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_backup);

        currentUser = BmobUser.getCurrentUser(UserBean.class);
        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("图片备份");

        loadImageMethods = new LoadImageMethods();
        View view = getWindow().getDecorView();
        context = this;

        currentUser = BmobUser.getCurrentUser(UserBean.class);
        userName = currentUser.getUsername();
        upDownloadMethods = new UpDownloadMethods(view, context, currentUser);

        fabUploadImage = (FloatingActionButton) findViewById(R.id.fab_upload_image);
        fabUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePath == null)
                    Toast.makeText(context, "请选择图片", Toast.LENGTH_LONG).show();
                else {
                    File file = new File(imagePath);
                    String name = file.getName();
                    String newName = name.replace(Config.JPG, "");
                    upDownloadMethods.uploadData(imagePath, newName,
                            Config.TYPE_IMG);
                }
            }
        });
        fabDownLoadImage = (FloatingActionButton) findViewById(R.id.fab_download_image);
        fabDownLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage();
            }
        });
        fabAddImage = (FloatingActionButton) findViewById(R.id.fab_add_pic);
        fabAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //获取图片路径
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            imagePath = c.getString(columnIndex);
            showImage(imagePath);
            c.close();
        }
    }

    private void showImage(String imgPath) {
        Bitmap bm = BitmapFactory.decodeFile(imgPath);
        ((ImageView) findViewById(R.id.img_shower)).setImageBitmap(bm);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void downloadImage() {
        GetInterDataMethods getInterDataMethods = new GetInterDataMethods();
        List<OSSObjectSummary> ossObjectSummaries = new ArrayList<OSSObjectSummary>();
        ossObjectSummaries.addAll(getInterDataMethods.GetDataFromOss(userName + "/" + Config.TYPE_IMG));//获取网站上图片数据
        LayoutInflater inflater = getLayoutInflater();

        View dialog = inflater.inflate(R.layout.dialog_online_photos, (ViewGroup) findViewById(R.id.liner_online_photos));
        AlertDialog alertDialogImageInterData = new AlertDialog.Builder(context).setView(dialog).create();
        mRecyclerViewInter = (RecyclerView) dialog.findViewById(R.id.online_photos);
        mRecyclerViewInter.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerViewAdapterInter = new RecyclerViewAdapter<OSSObjectSummary>(context, R.layout.item_online_photos, ossObjectSummaries) {
            @Override
            protected void convert(ViewHolder holder, OSSObjectSummary ossObjectSummary) {
                Picture picture = new Picture();
                picture.setKey(ossObjectSummary.getKey());
                BitmapFactory.Options opt = new BitmapFactory.Options();

                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                opt.inPurgeable = true;
                opt.inInputShareable = true;
                opt.inDither=false;

                picture.setSource(loadImageMethods.getPicFromBytes(loadImageMethods.asyncGetObjectSample(picture.getKey()), opt));
                pictures.add(picture);
                holder.setImageBitmap(R.id.item_of_photo, picture.getSource());
            }
        };

        mRecyclerViewAdapterInter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position) {
                upDownloadMethods.downloadFile(pictures.get(position).getKey(), Config.TYPE_IMG);
                alertDialogImageInterData.dismiss();
            }
        });
        mRecyclerViewInter.setAdapter(mRecyclerViewAdapterInter);
        alertDialogImageInterData.setTitle("云端图片文件");
        alertDialogImageInterData.setView(dialog);
        alertDialogImageInterData.show();
    }
}
