package com.example.mypc.cloudstorage.app;

/**
 * Created by My PC on 2018/3/12.
 */

public interface ProgressCallback<Request, Result> extends Callback<Request, Result> {
    void onProgress(Request request, long currentSize, long totalSize);
}
