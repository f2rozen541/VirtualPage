package com.hanvon.core;


/**
 * -------------------------------
 * Description:
 *
 * -------------------------------
 * Author:  hanlili
 * Date:    2016/2/24
 * E_mail:  hanlili@hanwang.com.cn
 */
import android.graphics.Color;

public class HWPen {

    private int mStyle;
    private int mColor;
    private int mWidth;
    private boolean mIsColor;
    private int[]mColors;

    public HWPen(int style, int color, int width)
    {
        mStyle = style;
        mColor = color;
        mWidth = width;
        mColors = null;
        mIsColor = false;
    }

    public HWPen(int style, int color, int width, boolean isColor)
    {
        mStyle = style;
        mWidth = width;
        if(isColor)
        {
            mColor = 6;
            mColors = new int[4];
            mColors[0] = Color.alpha(color);
            mColors[1] = Color.red(color);
            mColors[2] = Color.green(color);
            mColors[3] = Color.blue(color);
        }
        else {
            mColor = color;
            mColors = null;
        }

        mIsColor = isColor;
    }

    public void set(int style, int color, int width)
    {
        mStyle = style;
        mColor = color;
        mWidth = width;
    }

    public void set(HWPen pen)
    {
        mStyle = pen.getStyle();
        mColor = pen.getColor();
        mWidth = pen.getWidth();
        mIsColor = pen.getIsColor();
        if(mIsColor)
        {
            if(mColors == null)
                mColors = new int[4];

            mColors[0] = pen.getColors()[0];
            mColors[1] = pen.getColors()[1];
            mColors[2] = pen.getColors()[2];
            mColors[3] = pen.getColors()[3];
        }
        else {
            mColors = null;
        }
    }

    public int getStyle()
    {
        return mStyle;
    }

    public int getColor()
    {
        return mColor;
    }

    public int getWidth()
    {
        return mWidth;
    }

    public int[] getColors()
    {
        return mColors;
    }

    public int getTrueColor(){
        if(mColors != null)
            return Color.argb(mColors[0], mColors[1], mColors[2], mColors[3]);
        else
            return mColor;
    }

    public boolean getIsColor()
    {
        return mIsColor;
    }

    public void setStyle(int style)
    {
        mStyle = style;
    }

    public void setColor(int color)
    {
        mColor = color;
    }

    public void setWidth(int width)
    {
        mWidth = width;
    }
}

