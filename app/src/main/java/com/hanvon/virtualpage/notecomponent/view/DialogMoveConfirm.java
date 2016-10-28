package com.hanvon.virtualpage.notecomponent.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;
import com.hanvon.virtualpage.pageeditor.view.AutoBgButton;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/4/28
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DialogMoveConfirm extends BaseDialog implements View.OnClickListener {
    private View mParentView;
    private Context mContext;
    private AutoBgButton abbCopy;
    private AutoBgButton abbMove;
    private Button btnCancel;
    private Button btnOK;
    private int mChoice = 0;
    public static final int COPY_PAGE = 0;
    public static final int MOVE_PAGE = 1;
    private OnOptionListener mListener;

    public DialogMoveConfirm(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View getContentLayoutView() {
        initViews();
        return mParentView;
    }

//    public DialogMoveConfirm(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }

//    public DialogMoveConfirm(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        mContext = context;
//        mParentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_move_to_confirm_layout, this);
//        initViews();
//    }

    private void initViews() {
        mParentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_move_to_confirm_layout, null);
        abbCopy = (AutoBgButton) mParentView.findViewById(R.id.abb_copy);
        abbMove = (AutoBgButton) mParentView.findViewById(R.id.abb_move);
        btnCancel = (Button) mParentView.findViewById(R.id.tv_cancel);
        btnOK = (Button) mParentView.findViewById(R.id.tv_ok);
        btnCancel.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        abbCopy.setOnClickListener(this);
        abbMove.setOnClickListener(this);
        mChoice = MOVE_PAGE;
        abbMove.setSelectedState(true);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.abb_copy:
                mChoice = COPY_PAGE;
                abbCopy.setSelectedState(true);
                abbMove.setSelectedState(false);
                break;
            case R.id.abb_move:
                mChoice = MOVE_PAGE;
                abbCopy.setSelectedState(false);
                abbMove.setSelectedState(true);
                break;
            case R.id.tv_cancel:
                if (mListener != null) {
                    mListener.onCancelClick();
                }
                break;
            case R.id.tv_ok:
                if (mListener != null) {
                    mListener.onOKClick(mChoice);
                }
                break;
        }
    }

    public interface OnOptionListener {
        void onCancelClick();

        void onOKClick(int choice);
    }

    public void setOnOptionListener(OnOptionListener listener) {
        this.mListener = listener;
    }
}
