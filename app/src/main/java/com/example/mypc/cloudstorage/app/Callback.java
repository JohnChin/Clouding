package com.example.mypc.cloudstorage.app;

/**
 * Created by My PC on 2018/3/12.
 */

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;


public interface   Callback<Request, Result> {

    void onSuccess(Request request, Result result);

    void onFailure(Request request, ClientException clientException, ServiceException serviceException);
}
