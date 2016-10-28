package com.hanvon.virtualpage.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/5/23
 * @E_mail: taozhi@hanwang.com.cn
 */
public class AboutActivity extends Activity {
    private TextView tvVersionCode;
    private ImageView ivClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
//        BaseApplication.getApplication().addToActivityStack(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about);
        initView();
        BaseApplication.setSystemUiVisibility(this, true);
    }

    private void initView() {
        tvVersionCode = (TextView) findViewById(R.id.tv_version_code);
        tvVersionCode.setText(getVersionName());
//        tvVersionCode.setText(R.string.TestTagString);

        ivClose = (ImageView) findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public String getVersionName() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String verName = info.versionName;
            int verCode = info.versionCode;
            if (verName == null || verName.length() <= 0) {
                return "";
            }
            return verName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllViews();
    }

    private void releaseAllViews() {
//        ivClose = null;
//        tvVersionCode = null;
        setContentView(R.layout.layout_null_view);
        Runtime.getRuntime().gc();
        System.runFinalization();
    }


}
