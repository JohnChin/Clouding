package com.example.mypc.cloudstorage.methods;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.example.mypc.cloudstorage.app.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.mypc.cloudstorage.InitService.oss;

/**
 * Created by My PC on 2018/4/4.
 */

public class LoadImageMethods {
    private byte[] imageByteArray;

    public byte[] asyncGetObjectSample(String path) {

        GetObjectRequest get = new GetObjectRequest(Config.bucket, path);

        OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
            @Override
            public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                // 请求成功
                request.setxOssProcess("image/resize,m_lfit,p_50/quality,q_50");
                InputStream inputStream = result.getObjectContent();
                byte[] buffer = new byte[2048];
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                int len;

                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        // 处理下载的数据
                        outStream.write(buffer, 0, len);
                        Log.d("asyncGetObjectSample", "read length: " + len);
                    }
                    outStream.flush();
                    imageByteArray = outStream.toByteArray();
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
        task.waitUntilFinished();
        while (imageByteArray == null) {

        }
        return imageByteArray;
    }


    public Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null) {
            if (opts != null) {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
            } else {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } else {
        }
        return null;
    }

}


