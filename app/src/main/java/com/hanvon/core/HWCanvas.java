package com.hanvon.core;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;



/**
 * -------------------------------
 * Description:画布。根据指定宽，高，像素值，可以构造画布，
 * 并提供draw，clear等方法。
 * -------------------------------
 */
public class HWCanvas {
    private int[] mRect = new int[4];
    private int[] mBackgroundPixels;
    private byte[] mBackgroundPixelsByte;
    private int mBackgroundColor;

    private int mWidth;
    private int mHeight;

    private float[] mPoints = new float[2048];
    private float[] mPoints2 = new float[2048];
    private boolean mUsed = true;

    private com.hanvon.core.HWPen mPen;
    private boolean mIsSucsess;

    private float[] mCurrentPoints = null;

    /**
     * 构造函数
     *
     * @param width
     *            宽度
     * @param height
     *            高度
     * @param pixels
     *            当前操作的像素值数组
     * @param backgroundPixels
     *            背景像素值
     */
    public HWCanvas(int width, int height, int[] pixels, int[] backgroundPixels) {
        mBackgroundPixels = backgroundPixels;
        mBackgroundColor = Color.WHITE;
        mPen = new com.hanvon.core.HWPen(0, 0, 0);

        mWidth = width;
        mHeight = height;

        mBackgroundPixelsByte = null;

        mIsSucsess = HWColorPaint.initializeEx(width, height, pixels);
    }

    public HWCanvas(int width, int height, byte[] pixels, byte[] backgroundPixels) {
        mBackgroundPixelsByte = backgroundPixels;
        mBackgroundColor = Color.WHITE;
        mPen = new com.hanvon.core.HWPen(0, 0, 0);

        mWidth = width;
        mHeight = height;

        mBackgroundPixels = null;

        mIsSucsess = HWColorPaint.initializeExByte(width, height, pixels);
    }

    public void attach(int width, int height, int[] pixels, int[] backgroundPixels) {
        mWidth = width;
        mHeight = height;

        mBackgroundPixelsByte = null;
        mBackgroundPixels = backgroundPixels;
        mIsSucsess = HWColorPaint.initializeEx(width, height, pixels);
    }

    public void attach(int width, int height, byte[] pixels, byte[] backgroundPixels) {
        mWidth = width;
        mHeight = height;

        mBackgroundPixelsByte = backgroundPixels;
        mBackgroundPixels = null;
        mIsSucsess = HWColorPaint.initializeExByte(width, height, pixels);
    }

    public HWCanvas(int width, int height, int[] pixels, int color) {
        mBackgroundPixels = null;
        mBackgroundPixelsByte = null;
        mBackgroundColor = color;
        mPen = new com.hanvon.core.HWPen(0, 0, 0);

        mWidth = width;
        mHeight = height;

        mIsSucsess = HWColorPaint.initializeEx(width, height, pixels);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setPen(com.hanvon.core.HWPen pen) {
        if(!mIsSucsess)
            return;

        //if(!(mPen.getStyle() == pen.getStyle()
        //		&& mPen.getColor() == pen.getColor()
        //		&& mPen.getWidth() == pen.getWidth()))
        {
            HWColorPaint.setPen(3, pen.getStyle(), pen.getColor(), pen.getWidth(), 12 );
            if(pen.getIsColor())
                HWColorPaint.setPenColor(pen.getColors());
            mPen.set(pen);
        }
    }

    /**
     * 清空画布所画的东西
     */
    public void clear() {
        if(!mIsSucsess)
            return;

        addCilpRect(new Rect(0, 0, mWidth, mHeight));
        clearBackground();
        reSet();
    }

    public void clearBackground() {
        if(!mIsSucsess)
            return;

        if(mBackgroundPixels != null) {
            HWColorPaint.clearBackground(mBackgroundPixels);
        }
        else if(mBackgroundPixelsByte != null) {
            HWColorPaint.clearBackgroundByte(mBackgroundPixelsByte);
        }
        else {
            HWColorPaint.clear(mBackgroundColor);
        }
    }

    public void setBackground() {
        if(!mIsSucsess)
            return;

        if(mBackgroundPixels != null)
            HWColorPaint.setBackground(mBackgroundPixels);
        else
            HWColorPaint.clear(mBackgroundColor);
    }

    public void addCilpRect(Rect rc) {
        if(!mIsSucsess)
            return;

        mRect[0] = rc.left;
        mRect[1] = rc.top;
        mRect[2] = rc.right;
        mRect[3] = rc.bottom;

        HWColorPaint.addClipRegion(mRect);
    }

    public void setCilpRect(Rect rc) {
        if(!mIsSucsess)
            return;

        mRect[0] = rc.left;
        mRect[1] = rc.top;
        mRect[2] = rc.right;
        mRect[3] = rc.bottom;

        HWColorPaint.setClipRegion(mRect);
    }

    public void reSet() {
        if(!mIsSucsess)
            return;

        HWColorPaint.reSet();
    }

    /**
     * 为指定HWPath绘制一小段笔迹
     * 说明：一般是在MouseMove过程中调用
     *
     * @param path
     *            指定的HWPath
     *
     * @param pt
     *            采样点坐标
     *
     * @param pressure
     *            当前采样点 压感值
     *
     * @return    此次对像素点的操作矩阵区域
     */
    public Rect draw(com.hanvon.core.HWPath path, Point pt, float pressure) {
        Rect rc = new Rect();

        if(!mIsSucsess)
            return rc;

        //HWColorPen.drawLine(pt.x, pt.y, pressure, mRect, mPixels);
        float[] points = mPoints;
        if(mUsed) {
            mUsed = false;
        }
        else {
            points = mPoints2;
            mUsed = true;
        }
        if(points != null && points.length > 0) {
            points[0] = 0;
        }

        HWColorPaint.drawLineEx(pt.x, pt.y, pressure, mRect, points);
        //path.addData(points);
        path.add(pt, pressure);
        mCurrentPoints = points;

        mRect[0] = EnsureRange(mRect[0], 0, mWidth);
        mRect[1] = EnsureRange(mRect[1], 0, mHeight);
        mRect[2] = EnsureRange(mRect[2], 0, mWidth);
        mRect[3] = EnsureRange(mRect[3], 0, mHeight);

        path.updateBounds(mRect);
        rc.set(mRect[0], mRect[1], mRect[2], mRect[3]);

        return rc;
    }

    private int EnsureRange(int value, int min, int max) {
        if(min >= max) {
            return value;
        }

        if(value < min) {
            return min;
        }

        if(value > max) {
            return max;
        }

        return value;
    }

    public void UpdateData(com.hanvon.core.HWPath path) {
        if(mCurrentPoints != null)
            path.addData(mCurrentPoints);
    }

    /**
     * 重绘HWPath函数
     * 说明：一般是在笔迹擦擦除过程中笔迹重绘使用
     *
     * @param path
     *            包含所绘笔迹信息
     *
     * @param pen
     *            指定的HWPath
     */
    public void draw(com.hanvon.core.HWPath path, com.hanvon.core.HWPen pen, boolean isErase)
    {
        if(!mIsSucsess)
            return;

        setPen(pen);
        //注释部分：模拟压感
        //HWColorPaint.setSimulatePressure(true);
//        if(path.IsInterpolate())
            interpolate(path, isErase);
//        else
//        {
//            HWColorPaint.reDrawLineEx(path.PathData(), path.PathData().length, isErase);
//        }
    }

    private void interpolate(com.hanvon.core.HWPath path, boolean isErase)
    {
        if(path.PressureData().length == 0) {
            return;
        }
        int[] pRect = new int[4];
        float[] pathData = new float[path.PressureData().length * 1000];
        if(pathData != null && pathData.length > 0) {
            pathData[0] = 0;
        }
        HWColorPaint.interpolate(path.PointData(), path.PressureData(), pRect, pathData, isErase);
        path.updatePathData(pathData, pRect);
    }
}
