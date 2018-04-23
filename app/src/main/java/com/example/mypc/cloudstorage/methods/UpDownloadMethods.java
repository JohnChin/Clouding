package com.example.mypc.cloudstorage.methods;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.mypc.cloudstorage.InitService;
import com.example.mypc.cloudstorage.activities.ContactBackupActivity;
import com.example.mypc.cloudstorage.activities.ImageBackupActivity;
import com.example.mypc.cloudstorage.activities.ListAppBackupActivity;
import com.example.mypc.cloudstorage.activities.MessageBackupActivity;
import com.example.mypc.cloudstorage.app.Config;
import com.example.mypc.cloudstorage.bean.UserBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.bmob.v3.BmobUser;

import static com.example.mypc.cloudstorage.InitService.oss;

/**
 * Created by My PC on 2018/3/13.
 */

public class UpDownloadMethods {
    private Context context;
    private View view;
    private UserBean currentUser;
    private String userName;
    private ToolMethods toolMethods = new ToolMethods();

    public UpDownloadMethods(View view, Context context, UserBean userBean) {
        this.view = view;
        this.context = context;
        this.currentUser = userBean;
    }

    public void downloadFile(String name, String type) {
        String filepath = null;
        String newName;
        HandlerThread thread = new HandlerThread("NetWork");
        thread.start();
        Handler mHandler = new Handler(thread.getLooper());

        if (currentUser != null)
            userName = currentUser.getUsername() + "/";
        else {

        }
        if (type.equals(Config.TYPE_APK)) {
            newName = name.replace(userName + type, "");
            filepath = Config.APKS_PATH + "download_" + newName;
        } else if (type.equals(Config.TYPE_SMS)) {
            newName = name.replace(userName + type, "");
            filepath = Config.SMS_FILE_PATH + "download_" + newName;
        } else if (type.equals(Config.TYPE_CONTACT)) {
            newName = name.replace(userName + type, "");
            filepath = Config.CONTACT_FILE_PATH + "download_" + newName;
        } else if (type.equals(Config.TYPE_IMG)) {
            newName = name.replace(userName + type, "");
            filepath = Config.IMG_PATH + "download_" + newName;
        }

        File fileDownload = new File(filepath);
        fileDownload.getParentFile().mkdir();
        if (!fileDownload.exists()) {
            try {
                fileDownload.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ProgressDialog pDownloadDialog = new ProgressDialog(context);
        pDownloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDownloadDialog.setMessage(name + "  下载中...");
        pDownloadDialog.setCancelable(false);

        pDownloadDialog.show();


        GetObjectRequest get = new GetObjectRequest(Config.bucket, name);
        get.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
            @Override
            public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                pDownloadDialog.setProgress((int) (currentSize * (100.0 / totalSize)));
                pDownloadDialog.setProgressNumberFormat(toolMethods.FormatFileSize(currentSize) + "/" + toolMethods.FormatFileSize(totalSize));
                OSSLog.logDebug("getobj_progress: " + currentSize + "  total_size: " + totalSize, false);
            }
        });
        InitService.oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                // Log.d("Content-Length", "" + getResult.getContentLength());
                try {
                    FileOutputStream os = new FileOutputStream(fileDownload);
                    byte[] buf = new byte[1024];
                    InputStream in = result.getObjectContent();
                    int length = 0;
                    while ((length = in.read(buf)) > 0) {
                        os.write(buf, 0, length);
                    }
                    in.close();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pDownloadDialog.dismiss();
                Snackbar.make(view, name + " 下载成功！", Snackbar.LENGTH_LONG).show();


                if (type.equals(Config.TYPE_APK)) {
                    ListAppBackupActivity listAppBackupActivity = (ListAppBackupActivity) context;
                    listAppBackupActivity.RefreshList();
                } else if (type.equals(Config.TYPE_SMS)) {
                    MessageBackupActivity messageBackupActivity = (MessageBackupActivity) context;
                    messageBackupActivity.RefreshList();
                } else if (type.equals(Config.TYPE_CONTACT)) {
                    ContactBackupActivity contactBackupActivity = (ContactBackupActivity) context;
                    contactBackupActivity.RefreshList();
                } else if (type.equals(Config.TYPE_IMG)) {
                    ImageBackupActivity imageBackupActivity = (ImageBackupActivity) context;
                }

            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //如果上传失败了，通过mHandler ，发出失败的消息到主线程中。处理异常。
//                          showNetErrorInfo();
                    }
                });
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }
            }
        });
    }

    public void uploadData(String uploadFilePath, String name, String type) {
        Handler mHandler = new Handler();
        String LastName = null;
        if (currentUser != null)
            userName = currentUser.getUsername() + "/";
        else {

        }
        ProgressDialog pUploadDialog = new ProgressDialog(context);
        pUploadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pUploadDialog.setMessage(name + "上传中...");
        pUploadDialog.setCancelable(false);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                pUploadDialog.show();
            }
        });

        if (type.equals(Config.TYPE_APK))
            LastName = Config.APK;
        else if (type.equals(Config.TYPE_SMS) || type.equals(Config.TYPE_CONTACT))
            LastName = Config.XML;
        else if (type.equals((Config.TYPE_IMG)))
            LastName = Config.JPG;//设置上传文件类型

        PutObjectRequest put = new PutObjectRequest(InitService.OSS_BUCKET, userName + type + name + LastName, uploadFilePath);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                pUploadDialog.setProgress((int) (currentSize * (100.0 / totalSize)));
                pUploadDialog.setProgressNumberFormat(toolMethods.FormatFileSize(currentSize) + "/" + toolMethods.FormatFileSize(totalSize));
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });

        InitService.oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(final PutObjectRequest request, PutObjectResult result) {
                Log.d("PutObject", "UploadSuccess");
                Log.d("ETag", result.getETag());
                Log.d("RequestId", result.getRequestId());
                pUploadDialog.dismiss();
                Snackbar.make(view, name + " 上传成功！", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //如果上传失败了，通过mHandler ，发出失败的消息到主线程中。处理异常。
//                          showNetErrorInfo();
                    }
                });
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                }
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.getErrorCode());
                    Log.e("RequestId", serviceException.getRequestId());
                    Log.e("HostId", serviceException.getHostId());
                    Log.e("RawMessage", serviceException.getRawMessage());
                }

            }
        });
    }

}
