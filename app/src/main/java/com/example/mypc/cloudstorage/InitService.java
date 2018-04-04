package com.example.mypc.cloudstorage;

import android.app.Application;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss_android_sdk.BuildConfig;
import com.example.mypc.cloudstorage.app.Config;

import cn.bmob.v3.Bmob;

/**
 * Created by My PC on 2018/3/12.
 */

public class InitService extends Application {
    public static final String OSS_BUCKET = "allapk";
    //设置OSS数据中心域名或者cname域名
    private String apkEndpoint = "http://oss-cn-beijing.aliyuncs.com";
    //Key
    private String accessKeyId="LTAIDyi6xj4eSJKM";
    private String secretKeyId="JjMy7Xdwh6v4Nuo0EGjystA1Oleh6u";
    public static OSS oss;
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化OSS配置
        initOSSConfig();
        initBomb();
    }
    private void initOSSConfig(){
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, secretKeyId);
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        if(BuildConfig.DEBUG){
            OSSLog.enableLog();
        }
        oss = new OSSClient(getApplicationContext(), Config.endpoint, credentialProvider, conf);
    }

    private void initBomb(){
        Bmob.initialize(this, "07046f731c4d6241ce345adbece8785c");
    }
}
