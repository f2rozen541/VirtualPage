package com.hanvon.virtualpage.common;

import android.util.DisplayMetrics;

import com.hanvon.virtualpage.BaseApplication;

/**
 * -------------------------------
 * Description:
 * <p/> 常用资源管理类，用于资源统一管理
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/3/10
 * E_mail:  taozhi@hanwang.com.cn
 */
public class ConstantValues {
    private static int screenWidth = 1920;
    private static int screenHeight = 1200;
    private static int[] strokeViewPixels;
    private static int[] strokeViewBgPixels;
//    private static Bitmap sBitmap;


//    private static ConstantValues sInstance = null;
    private ConstantValues() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        BaseApplication.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }
    public static final ConstantValues getInstance(){
//        if(null == sInstance){
//            synchronized (ConstantValues.class){
//                if(null == sInstance){
//                    sInstance = new ConstantValues();
//
//                }
//            }
//        }
//        return sInstance;
        return SingletonHolder.sInstance;
    }

    // 只有在装载该内部类的时候才会去创建单例对象
    private static class SingletonHolder {
        public static ConstantValues sInstance = new ConstantValues();
    }

    public int[] getStrokeViewPixels() {
        if (strokeViewPixels == null) {
            strokeViewPixels = new int[screenWidth * screenHeight];
        } else {
            for (int index = 0; index < strokeViewPixels.length; index++) {
                strokeViewPixels[index] = 0;
            }
        }
        return strokeViewPixels;
    }

    public int[] getStrokeViewBgPixels() {
        if (strokeViewBgPixels == null) {
            strokeViewBgPixels = new int[screenWidth * screenHeight];
        } else {
            for (int index = 0; index < strokeViewBgPixels.length; index++) {
                strokeViewBgPixels[index] = 0;
            }
        }
        return strokeViewBgPixels;
    }

    public void releaseStrokeViewArray() {
        strokeViewPixels = null;
        strokeViewBgPixels = null;
    }


}
