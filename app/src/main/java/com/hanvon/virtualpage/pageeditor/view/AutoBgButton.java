package com.hanvon.virtualpage.pageeditor.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.hanvon.virtualpage.R;

/**
 * -------------------------------
 * Description:
 * 自定义Button，主要是为了实现点击后，背景智能改变的效果
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/2/22
 * E_mail:  taozhi@hanwang.com.cn
 */
public class AutoBgButton extends ImageButton {
    private int unSelectedBg;
    private int selectedBg;
    private boolean selectState = false;
    private int currentColor;
    public AutoBgButton(Context context) {
        this(context, null);
    }

    public AutoBgButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoBgButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AutoBgButton, defStyleAttr, 0);
        unSelectedBg = a.getResourceId(R.styleable.AutoBgButton_UnSelectedBgRes, -1);
        selectedBg = a.getResourceId(R.styleable.AutoBgButton_SelectedBgRes, -1);

        Drawable drawable = a.getDrawable(Resources.getSystem().getIdentifier("View_background","styleable","android"));
        if (drawable instanceof ShapeDrawable){
            currentColor = ((ShapeDrawable) drawable).getPaint().getColor();
        }
        a.recycle();
//        setBackground(unSelectedBg);

    }

    public void setBgColor(int color) {
        currentColor = color;
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        shapeDrawable.setBounds(0, 0, 46, 46);
        this.setBackground(shapeDrawable);
    }
    public int getBgColor(){
        return currentColor;
    }
    /**
     * 根据给定的选中状态设置控件的背景
     *
     * @param isSelect 选中状态
     */
    public void setSelectedState(boolean isSelect) {
        selectState = isSelect;
        if (selectState) {
            this.setImageResource(selectedBg);
        } else {
            this.setImageResource(unSelectedBg);
        }
    }

    public boolean getSelectState() {
        return selectState;
    }

    public void setUnSelectedBg(int resId) {
        this.unSelectedBg = resId;
    }

    public void setSelectedBg(int resId) {
        this.selectedBg = resId;
    }
}
