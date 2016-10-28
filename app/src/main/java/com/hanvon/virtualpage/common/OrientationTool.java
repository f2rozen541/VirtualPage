package com.hanvon.virtualpage.common;

import android.content.Context;
import android.view.OrientationEventListener;

import com.orhanobut.logger.Logger;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/5
 * E_mail:  taozhi@hanwang.com.cn
 */
public class OrientationTool extends OrientationEventListener {
    public OrientationTool(Context context) {
        super(context);
    }

    public OrientationTool(Context context, int rate) {
        super(context, rate);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        Logger.e("OrientationTool====>" + orientation);
//        disable();
    }
}
