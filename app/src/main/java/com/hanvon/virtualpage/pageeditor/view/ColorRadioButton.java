package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.hanvon.virtualpage.R;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/19
 * E_mail:  taozhi@hanwang.com.cn
 */
public class ColorRadioButton extends RadioButton {

    private int radioButtonColor;

    public static final int CIRCLE_WIDTH = 3;

    private int mWidth;
    private boolean isChecked = false;
    private int mHeight;

    public ColorRadioButton(Context context) {
        this(context, null);
    }

    public ColorRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ColorRadioButton, defStyleAttr, 0);
        radioButtonColor = a.getColor(0, 0x000000);
        a.recycle();
        isChecked = isChecked();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = mWidth / 2;
        int centerY = mHeight / 2;

        float radius = centerX * 0.7f;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(radioButtonColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, centerX, paint);


//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(centerY - radius);
//        // 先画外圆
//        canvas.drawCircle(centerX, centerY, radius, paint);
//        // 判断是否被选中修改内圆半径
//        if(isChecked){
//            radius = radius - CIRCLE_WIDTH;
//        }
//        paint.setStyle(Paint.Style.FILL);
//        // 画内圆
//        canvas.drawCircle(centerX, centerY, radius, paint);

    }
}
