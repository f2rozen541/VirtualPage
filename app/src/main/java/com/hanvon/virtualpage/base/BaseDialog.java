package com.hanvon.virtualpage.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.hanvon.virtualpage.R;

/**
 * @Description:
 *      无关主题，让Dialog能够默认半透明状态的基类
 * @Author: TaoZhi
 * @Date: 2016/6/2
 * @E_mail: taozhi@hanwang.com.cn
 */
public abstract class BaseDialog extends Dialog {

    private FrameLayout parentView;
    private Context mContext;
    private View mContentView;

    public BaseDialog(Context context) {
        super(context, R.style.DialogTranslucentBg);
        mContext = context;
    }

    public BaseDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        parentView = new FrameLayout(mContext);
        mContentView = getContentLayoutView();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                (int) mContext.getResources().getDimension(R.dimen.dialog_width),
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        parentView.addView(mContentView, params);
        setContentView(parentView);
//        setContentView(parentView,
//                new ViewGroup.LayoutParams(
//                        mContext.getResources().getDisplayMetrics().widthPixels,
//                        mContext.getResources().getDisplayMetrics().heightPixels));
    }

    /**
     * 需要显示的View， 必须被实现
     * @return 将要被显示的View
     */
    public abstract View getContentLayoutView();

}
