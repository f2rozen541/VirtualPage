package com.hanvon.virtualpage.notecomponent.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/8
 * E_mail:  taozhi@hanwang.com.cn
 */
public class DialogProgress extends BaseDialog {

    private Context mContext;
    private View parentView;
    private MagicProgressBar magicProgressBar;
    private TextView tvStop;
    private OnStopClickListener mListener;

    public DialogProgress(Context context) {
        super(context);
        mContext = context;

    }

    public DialogProgress(Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    public View getContentLayoutView() {
        initView();
        return parentView;
    }

    private void initView() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress, null);
        magicProgressBar = (MagicProgressBar) parentView.findViewById(R.id.mpb_progress);
        tvStop = (TextView) parentView.findViewById(R.id.tv_stop);
        tvStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onStopClick();
                }
            }
        });
    }

    public void setProgress(float value) {
        magicProgressBar.setPercent(value);
    }

    public void setSmoothProgress(float value) {
        magicProgressBar.setSmoothPercent(value);
    }

    public void setSmoothProgress(float value, long durationMillis) {
        magicProgressBar.setSmoothPercent(value, durationMillis);
    }

    public interface OnStopClickListener{
        void onStopClick();
    }
    public void setOnStopClickListener(OnStopClickListener listener) {
        mListener = listener;
    }
}
