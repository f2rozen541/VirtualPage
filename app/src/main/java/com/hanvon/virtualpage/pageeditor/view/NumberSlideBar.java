package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hanvon.virtualpage.R;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/23
 * E_mail:  taozhi@hanwang.com.cn
 */
public class NumberSlideBar extends FrameLayout {

    private View parentView;
    private TextView tv_icon;
    private ImageView iv_progress;
    private OnProgressChangeListener mOnProgressChangeListener;

    private int startX = 0;
    private int deltaX = 0;
    private ViewGroup.LayoutParams progressLayoutParams;
    private int mParentWidth;
    private int minProgressWidth;
    private int maxProgressWidth;
    private int maxTextSize;
    private int minTextSize;
    private float sectionValue;
    private int mProgressValue;
    private boolean hasSetProgress;

    public NumberSlideBar(Context context) {
        this(context, null);
    }

    public NumberSlideBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberSlideBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentView = LayoutInflater.from(context).inflate(R.layout.slidebar_layout, this);
        initViews();
        initData();
}

    private void initData() {
        maxTextSize = 100;
        minTextSize = 20;
        sectionValue = maxTextSize - minTextSize;
        mProgressValue = -1;
        hasSetProgress = false;
    }

    /**
     * 可以用来设置字体的变化范围，默认是20-100
     * @param min 最小值
     * @param max 最大值
     */
    public void setTextSizeBoundary(int min, int max){
        minTextSize = min;
        maxTextSize = max;
        sectionValue = maxTextSize - minTextSize;
    }

    public void setProgress(int value) {
        if(value < minTextSize){
            value = minTextSize;
        }
        if(value > maxTextSize){
            value = maxTextSize;
        }
        mProgressValue = value;
        hasSetProgress = true;
    }

    public int getProgress(){
        return mProgressValue;
    }

    private void initViews() {
        tv_icon = (TextView) parentView.findViewById(R.id.btn_icon);
        iv_progress = (ImageView) parentView.findViewById(R.id.iv_progress);
        tv_icon.setOnTouchListener(new IconOnTouchListener());
    }

    private void updateProgress(int delta) {
        progressLayoutParams = iv_progress.getLayoutParams();
        // 计算进度条宽度值
        int newWidth = progressLayoutParams.width + delta;
        if(newWidth > mParentWidth){
            newWidth = mParentWidth;
        }else if(newWidth < minProgressWidth){
            newWidth = minProgressWidth;
        }
        progressLayoutParams.width = newWidth;
        // 设置数字显示
        float percentage = (newWidth - minProgressWidth) * 1.0f / maxProgressWidth; // 计算当前位置的比例值
        mProgressValue = minTextSize + (int)(sectionValue * percentage);

        tv_icon.setText(String.valueOf(mProgressValue)); // 设置数值
        iv_progress.setLayoutParams(progressLayoutParams); // 设置进度条宽度

        if (mOnProgressChangeListener != null){
            mOnProgressChangeListener.ProgressChanged(mProgressValue);
        }
    }

    /**
     * 图标拖动的监听类
     */
    private class IconOnTouchListener implements OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    startX = (int) event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    deltaX = (int) (event.getRawX() - startX);
                    updateProgress(deltaX);
                    startX = (int) event.getRawX();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mParentWidth = MeasureSpec.getSize(widthMeasureSpec);
        minProgressWidth = tv_icon.getMeasuredWidth();

        maxProgressWidth = mParentWidth - minProgressWidth;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if(mProgressValue == -1 || hasSetProgress){ // 如果是第一次加载或者设置了新的进度值，就刷新显示
            if(mProgressValue == -1){
                mProgressValue = minTextSize;
            }
            tv_icon.setText(String.valueOf(mProgressValue)); // 设置数值

            int span = mProgressValue - minTextSize;
            float percentage = span / sectionValue;
            int progressWidth = (int) (minProgressWidth + maxProgressWidth * percentage);
            progressLayoutParams = iv_progress.getLayoutParams();
            progressLayoutParams.width = progressWidth;
            iv_progress.setLayoutParams(progressLayoutParams); // 设置进度条宽度

            hasSetProgress = false;
        }
    }

    public interface OnProgressChangeListener {
        void ProgressChanged(int value);
    }

    public void setProgressChangeListener(OnProgressChangeListener listener) {
        this.mOnProgressChangeListener = listener;
    }
}
