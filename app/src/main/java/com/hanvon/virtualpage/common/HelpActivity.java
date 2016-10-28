package com.hanvon.virtualpage.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.utils.SpannableUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @Description:
 * @Author: TaoZhi
 * @Date: 2016/5/21
 * @E_mail: taozhi@hanwang.com.cn
 */
public class HelpActivity extends Activity {

    private ViewPager vpContent;
//    private Context mContext;
    private ArrayList<View> mViewList;
    private PagerAdapter mPagerAdapter;
    private LinearLayout llTabs;
    private ImageView ivClose;
//    private LayoutInflater inflater;
    private View step1;
    private View step2;
    private View step3;
    private View step4;
    private View step5;
    private View step6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
        initViews();
        BaseApplication.setSystemUiVisibility(this, true);
    }

    private void initViews() {
        llTabs = (LinearLayout) findViewById(R.id.ll_tabs);
        ivClose = (ImageView) findViewById(R.id.iv_close);
        vpContent = (ViewPager) findViewById(R.id.vp_help_content);
        mViewList = new ArrayList<>();
        step1 = getLayoutInflater().inflate(R.layout.help_step_1, null);
//        Logger.e("第1步完成！");
        step2 = getLayoutInflater().inflate(R.layout.help_step_2, null);
//        Logger.e("第2步完成！");
        step3 = getLayoutInflater().inflate(R.layout.help_step_3, null);
//        Logger.e("第3步完成！");
        step4 = getLayoutInflater().inflate(R.layout.help_step_4, null);
//        Logger.e("第4步完成！");
        step5 = getLayoutInflater().inflate(R.layout.help_step_5, null);
//        Logger.e("第5步完成！");
        step6 = getLayoutInflater().inflate(R.layout.help_step_6, null);
//        Logger.e("第6步完成！");
        mViewList.add(step1);
        mViewList.add(step2);
        mViewList.add(step3);
        mViewList.add(step4);
        mViewList.add(step5);
        mViewList.add(step6);
//        mViewList.add(inflateFromLayout(R.layout.help_step_1));
//        mViewList.add(inflateFromLayout(R.layout.help_step_2));
//        mViewList.add(inflateFromLayout(R.layout.help_step_3));
//        mViewList.add(inflateFromLayout(R.layout.help_step_4));
//        mViewList.add(inflateFromLayout(R.layout.help_step_5));
//        mViewList.add(inflateFromLayout(R.layout.help_step_6));
        mPagerAdapter = new PagerAdapter() {

            @Override
            public int getCount() {
                return mViewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = mViewList.get(position);
                ImageView ivShowImg = (ImageView) itemView.findViewById(R.id.iv_showImg);
                container.addView(itemView);
                switch (position) {
                    case 0:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_1);
                        Glide.with(HelpActivity.this)
                                .load(R.drawable.img_help_step_1)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);
                        break;
                    case 1:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_2);
                        Glide.with(HelpActivity.this)
                                .load(R.drawable.img_help_step_2)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);
                        EditText etDetail_1 = (EditText) itemView.findViewById(R.id.et_detail);
                        String detail_1 = etDetail_1.getText().toString();
                        if (detail_1.contains("@hanvon@")) {
                            int startIndex = detail_1.indexOf("@hanvon@");
                            if (startIndex >= 0) {
                                Drawable drawable = BaseApplication.getContext().getResources().getDrawable(R.drawable.ic_help_step2_setting_light, null);
                                drawable.setBounds(0, 0, 64, 64);
                                int endIndex = startIndex + 8;
                                SpannableString spannableString = SpannableUtils.setTextImg(detail_1, startIndex, endIndex, drawable);
                                etDetail_1.setText(spannableString);
                            }
                        }
                        break;
                    case 2:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_3);
                        Glide.with(HelpActivity.this)
                                .load(R.drawable.img_help_step_3)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);

                        break;
                    case 3:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_4);
                        Glide.with(HelpActivity.this)
                                .load(R.drawable.img_help_step_4)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);
                        EditText etDetail_3 = (EditText) itemView.findViewById(R.id.et_detail);
                        String detail_3 = etDetail_3.getText().toString();
                        if (detail_3.contains("@hanvon@")) {
                            int startIndex = detail_3.indexOf("@hanvon@");
                            if (startIndex >= 0) {
                                Drawable drawable = BaseApplication.getContext().getResources().getDrawable(R.drawable.ic_help_step4_merge_light, null);
                                drawable.setBounds(0, 0, 64, 64);
                                int endIndex = startIndex + 8;
                                SpannableString spannableString = SpannableUtils.setTextImg(detail_3, startIndex, endIndex, drawable);
                                etDetail_3.setText(spannableString);
                            }
                        }
                        break;
                    case 4:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_5);
                        Glide.with(HelpActivity.this)
                                //.load(R.drawable.img_help_step_5)
                                .load(isRTL() ? R.drawable.img_help_step_5_arab : R.drawable.img_help_step_5)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);
                        break;
                    case 5:
//                        ivShowImg.setBackgroundResource(R.drawable.img_help_step_6);
                        Glide.with(HelpActivity.this)
                                .load(R.drawable.img_help_step_6)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(ivShowImg);
                        break;
                    default:
                        break;
                }
                return itemView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View itemView = mViewList.get(position);
                if (itemView != null) {
                    ImageView ivShowImg = (ImageView) itemView.findViewById(R.id.iv_showImg);
                    ivShowImg.setBackground(null);
                    container.removeView(itemView);
                }
            }
        };
        vpContent.setOffscreenPageLimit(1);
        vpContent.setAdapter(mPagerAdapter);
        updateTabFocus(0);
        vpContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateTabFocus(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean isRTL() {
        return Locale.getDefault().getLanguage().equals("iw") || Locale.getDefault().getLanguage().equals("ar");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllViews();
    }

    private void releaseAllViews() {
//        ivClose = null;
        if (vpContent != null) {
//            int childCount = vpContent.getChildCount();
//            for (int i = childCount; i > 0; i--) {
//                vpContent.getChildAt(childCount).findViewById(R.id.iv_showImg).setBackground(null);
//            }
            vpContent.removeAllViews();
            vpContent = null;
        }

        if (mViewList != null) {
            mViewList.clear();
            mViewList = null;
        }
        step1 = null;
        step2 = null;
        step3 = null;
        step4 = null;
        step5 = null;
        step6 = null;
        llTabs = null;

        mPagerAdapter = null;

        setContentView(R.layout.layout_null_view);

        Runtime.getRuntime().gc();
        System.runFinalization();
    }

    //    private View inflateFromLayout(int layoutId) {
//        View inflate = inflater.inflate(layoutId, null);
//        if (layoutId == R.layout.help_step_2) {
//            EditText etDetail = (EditText) inflate.findViewById(R.id.et_detail);
//            String detail = etDetail.getText().toString();
//            int startIndex = detail.indexOf("@hanvon@");
//            if (startIndex >= 0) {
//                Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_help_step2_setting_light, null);
//                drawable.setBounds(0, 0, 64, 64);
//                int endIndex = startIndex + 8;
//                SpannableString spannableString = SpannableUtils.setTextImg(detail, startIndex, endIndex, drawable);
//                etDetail.setText(spannableString);
//            }
//
//        } else if (layoutId == R.layout.help_step_4) {
//            EditText etDetail = (EditText) inflate.findViewById(R.id.et_detail);
//            String detail = etDetail.getText().toString();
//            int startIndex = detail.indexOf("@hanvon@");
//            if (startIndex >= 0) {
//                Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_help_step4_merge_light, null);
//                drawable.setBounds(0, 0, 64, 64);
//                int endIndex = startIndex + 8;
//                SpannableString spannableString = SpannableUtils.setTextImg(detail, startIndex, endIndex, drawable);
//                etDetail.setText(spannableString);
//            }
//        }
//        return inflate;
//    }

    private void updateTabFocus(int position) {
        for (int i = 0; i < mViewList.size(); i++) {
            if (i == position) {
                llTabs.getChildAt(i).setBackgroundResource(R.drawable.ic_help_tab_indicator_selected_light);
            } else {
                llTabs.getChildAt(i).setBackgroundResource(R.drawable.ic_help_tab_indicator_rest_light);
            }
        }
    }
}
