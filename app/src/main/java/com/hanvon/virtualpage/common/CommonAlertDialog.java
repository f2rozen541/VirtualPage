package com.hanvon.virtualpage.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;

/**
 * -------------------------------
 * Description:
 * <p/> 通用弹框类
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/6
 * E_mail:  taozhi@hanwang.com.cn
 */
public class CommonAlertDialog extends BaseDialog {

    private Context mContext;
    private View parentView;
    private TextView tvTitle;
    private TextView tvContent;
    private TextView tvCancel;
    private TextView tvConfirm;
    private OnDialogOperationListener mListener;

    public CommonAlertDialog(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    @Override
    public View getContentLayoutView() {
        return parentView;
    }

    private void initViews() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_common_alert, null);
        tvTitle = (TextView) parentView.findViewById(R.id.tv_title);
        tvContent = (TextView) parentView.findViewById(R.id.tv_content);
        tvCancel = (TextView) parentView.findViewById(R.id.tv_cancel);
        tvConfirm = (TextView) parentView.findViewById(R.id.tv_ok);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                dismiss();
            }
        });
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirmClick();
                }
                dismiss();
            }
        });
    }

    public CommonAlertDialog setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public CommonAlertDialog setContentInfo(String contentInfo) {
        tvContent.setText(contentInfo);
        return this;
    }

    public interface OnDialogOperationListener {
        void onCancelClick();
        void onConfirmClick();
    }
    public void setOnDialogClickListener(OnDialogOperationListener listener) {
        mListener = listener;
    }
}
