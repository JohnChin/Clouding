package com.example.mypc.cloudstorage.methods;

import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.OSSObjectSummary;
import com.example.mypc.cloudstorage.app.Config;

import java.util.ArrayList;
import java.util.List;

import static com.example.mypc.cloudstorage.InitService.oss;

/**
 * Created by My PC on 2018/3/14.
 */

public class GetInterDataMethods {
    public List<OSSObjectSummary> GetDataFromOss(String typePath){
        ListObjectsRequest listObjects = new ListObjectsRequest(Config.bucket);
        List<OSSObjectSummary> ossObjectSummaries=new ArrayList<>();
        listObjects.setPrefix(typePath);
        // 设置成功、失败回调，发送异步罗列请求
        OSSAsyncTask task = oss.asyncListObjects(listObjects, new OSSCompletedCallback<ListObjectsRequest, ListObjectsResult>() {
            @Override
            public void onSuccess(ListObjectsRequest request, ListObjectsResult result) {
                Log.d("AyncListObjects", "Success!");
                for (int i = 0; i < result.getObjectSummaries().size(); i++) {

                    Log.d("AyncListObjects", "object: " + result.getObjectSummaries().get(i).getKey() + " "
                            + result.getObjectSummaries().get(i).getETag() + " "
                            + result.getObjectSummaries().get(i).getLastModified());
                }
                ossObjectSummaries.addAll(result.getObjectSummaries());
            }
            @Override
            public void onFailure(ListObjectsRequest request, ClientException clientExcepion, ServiceException serviceException) {
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
        return ossObjectSummaries;
    }
}
