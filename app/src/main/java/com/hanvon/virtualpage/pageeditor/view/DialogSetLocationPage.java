package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.base.BaseDialog;
import com.hanvon.virtualpage.beans.UIConstants;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/6/2
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DialogSetLocationPage extends BaseDialog implements View.OnClickListener {

    private View parentView;
    private AutoBgButton abbJumpLatest;
    private AutoBgButton abbJumpEnd;
    private TextView tvCancel;
    private TextView tvnOk;
    private int mLocatePosition;
    private Context mContext;

    private SharedPreferences sp;
    public DialogSetLocationPage(Context context) {
        super(context);
        mContext = context;
        initViews();
        initData();
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        mContext = getContext();
//        initViews();
//        initData();
//    }

    @Override
    public View getContentLayoutView() {
        return parentView;
    }

    private void initData() {
        sp = BaseApplication.getAppSharePreferences();
        mLocatePosition = sp.getInt(UIConstants.LOCATE_PAGE_POSITION, UIConstants.JUMP_TO_END);
        setLocatePosition(mLocatePosition);
    }

    private void initViews() {
        parentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_setting_open_page_position, null);
        abbJumpLatest = (AutoBgButton) parentView.findViewById(R.id.abb_jump_latest);
        abbJumpEnd = (AutoBgButton) parentView.findViewById(R.id.abb_jump_to_end);
        tvCancel = (TextView) parentView.findViewById(R.id.tv_cancel);
        tvnOk = (TextView) parentView.findViewById(R.id.tv_ok);
        abbJumpLatest.setOnClickListener(this);
        abbJumpEnd.setOnClickListener(this);
        tvnOk.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
//        this.setContentView(parentView, new ViewGroup.LayoutParams(
//                mContext.getResources().getDisplayMetrics().widthPixels,
//                mContext.getResources().getDisplayMetrics().heightPixels));
    }

    public void setLocatePosition(int toPosition) {
        this.mLocatePosition = toPosition;
        if (mLocatePosition == UIConstants.JUMP_TO_END) {
            abbJumpEnd.setSelectedState(true);
            abbJumpLatest.setSelectedState(false);
        } else {
            abbJumpEnd.setSelectedState(false);
            abbJumpLatest.setSelectedState(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.abb_jump_to_end:
                setLocatePosition(UIConstants.JUMP_TO_END);
                break;
            case R.id.abb_jump_latest:
                setLocatePosition(UIConstants.JUMP_TO_LATEST);
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_ok:
                sp.edit().putInt(UIConstants.LOCATE_PAGE_POSITION, mLocatePosition).commit();
                dismiss();
                break;
            default:
                break;
        }
    }
}
