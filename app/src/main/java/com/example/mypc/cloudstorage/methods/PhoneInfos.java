package com.example.mypc.cloudstorage.methods;

/**
 * Created by My PC on 2018/4/22.
 */
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.WindowManager;

/**
 * 读取手机设备信息测试代码
 */
public class PhoneInfos {

    private static TelephonyManager tm;

    /**
     * 获取SIM硬件信息
     *
     * @return
     */
    public static TelephonyManager getTelephonyManager(Context context) {
        if (tm == null)
            tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm;
    }
    /**
     * 获取屏幕分辨率
     *
     * @return
     */
    public static int[] getMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int[] metrics = {width, height};
        return metrics;
    }

    /**
     * 设备厂商
     *
     * @return
     */
    public static String getPhoneBrand() {
        return Build.BOARD + "  " + Build.MANUFACTURER;
    }

    /**
     * 设备名称
     *
     * @return
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }
}