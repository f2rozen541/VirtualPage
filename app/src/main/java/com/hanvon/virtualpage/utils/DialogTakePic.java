package com.hanvon.virtualpage.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;

/**
 * Created by hanvon1 on 16-4-20.
 */
public class DialogTakePic extends Dialog {

    private Context mContext;
    private LinearLayout mLayout;
    private View.OnClickListener onClickListener;

    public DialogTakePic(Context context) {
        super(context, R.style.shareDialogTheme);
        mContext = context;
    }

    public DialogTakePic(Context context, int themeResId) {
        this(context);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getApplication().setSystemUiVisibility1(this, true);
        init(mContext);
        setContentView(this.mLayout);
        getWindow().setGravity(80);
    }

    public void setOnDialogTakePicListener(View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }

    private void init(final Context context) {
        this.mLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.page_take_pickture, null);
        LinearLayout takePhoto = (LinearLayout) mLayout.findViewById(R.id.ll_take_photo);
        LinearLayout choseImage = (LinearLayout) mLayout.findViewById(R.id.ll_chose_image);
//        if (BaseApplication.specifiedLocate(context, "ru") == true) { // 为俄语单独修改字体类型为Roboto
//            TextView tvTitle = (TextView) mLayout.findViewById(R.id.tv_title);
//            TextView tvTakePhoto = (TextView) mLayout.findViewById(R.id.tv_take_photo);
//            TextView tvChooseImage = (TextView) mLayout.findViewById(R.id.tv_choose_image);
//            Typeface roboto = Typeface.create("monospace", Typeface.NORMAL);
//            tvTitle.setTypeface(roboto);
//            tvTakePhoto.setTypeface(roboto);
//            tvChooseImage.setTypeface(roboto);
//        }
        if (onClickListener != null){
            takePhoto.setOnClickListener(onClickListener);
            choseImage.setOnClickListener(onClickListener);
        }
    }
}
