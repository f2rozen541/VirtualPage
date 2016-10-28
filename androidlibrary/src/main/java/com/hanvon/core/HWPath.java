package com.hanvon.core;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------
 * Description:
 *
 * -------------------------------
 * Author:  hanlili
 * Date:    2016/2/24
 * E_mail:  hanlili@hanwang.com.cn
 */
public class HWPath {

    List<Float> mData = new ArrayList<Float>();
    //表示当前路径是否包含插值信息
    boolean mIsInterpolate;

    Rect mBounds = new Rect();

    List<Point> mPoints = new ArrayList<Point>();
    List<Float> mPrs = new ArrayList<Float>();

    int[] mPts;
    float[] mPressures;
    float[] mPathData;

    public HWPath()
    {
        mIsInterpolate = true;
    }

    /**
     * 构造函数
     * 说明：一般是保存后打开或者上一页下一页中使用
     *
     * @param pts
     *            点坐标
     * @param prs
     *            压感
     */
    public HWPath(List<Point> pts, List<Float> prs)
    {
        int size = pts.size();
        int indexPoint = 0;

        mPoints = pts;
        mPrs = prs;

        mPts = new int[size*2];
        mPressures = new float[size];

        int minX = 0, minY = 0, maxX = 0, maxY = 0, x, y;

        for(int i = 0; i< size; i++)
        {
            x = mPoints.get(i).x;
            y = mPoints.get(i).y;

            if(i == 0)
            {
                minX = x;
                minY = y;
                maxX = x;
                maxY = y;
            }
            else
            {
                minX = Math.min(x, minX);
                minY = Math.min(y, minY);
                maxX = Math.max(x, maxX);
                maxY = Math.max(y, maxY);
            }

            mPts[indexPoint++] = x;
            mPts[indexPoint++] = y;
            mPressures[i] = mPrs.get(i);
        }

        mBounds.set(minX, minY, maxX, maxY);
        mBounds.inset(-12, -12);

        mIsInterpolate = true;
    }

    public boolean endPath() {
        int size = mPoints.size();
        //自动补结尾
        if(size == 0) {
            size = 0;
        }
        if(size >= 1 && (mPoints.get(size - 1).x != StrokeCollection.ENDPOINT.x
                || mPoints.get(size - 1).y != StrokeCollection.ENDPOINT.y))
            return true;

        return false;
    }

    public boolean IsInterpolate()
    {
        return mIsInterpolate;
    }

    public Point LastPt() {
        if(mPoints.size()<1)
            return null;
        else
            return mPoints.get(mPoints.size()-1);
    }

    public float LastPressure() {
        if(mPrs.size()<1)
            return -1;
        else
            return mPrs.get(mPoints.size()-1);
    }

    public Rect getBounds()
    {
        return mBounds;
    }

    public void setBounds(Rect rect)
    {
        mBounds = rect;
    }

    public int[] PointData() {
        if(mPts == null)
        {
            int indexPoint = 0;
            int size = mPoints.size();
            Point pt = mPoints.get(size-1);

            if(pt.x != StrokeCollection.ENDPOINT.x && pt.y != StrokeCollection.ENDPOINT.y)
            {
                mPoints.add(StrokeCollection.ENDPOINT);
                size += 1;
            }

            mPts = new int[size*2];
            for(int i = 0; i< size; i++)
            {
                mPts[indexPoint++] = mPoints.get(i).x;
                mPts[indexPoint++] = mPoints.get(i).y;
            }
        }
        return mPts;
    }

    public float[] PressureData() {
        if(mPressures == null) {
            int size = mPrs.size();

            if(mPrs.get(size - 1) != StrokeCollection.ENDPRESSURE) {
                mPrs.add(StrokeCollection.ENDPRESSURE);
                size += 1;
            }

            mPressures = new float[size];

            for(int i = 0; i< size; i++) {
                mPressures[i] = mPrs.get(i);
            }
        }
        return mPressures;
    }

    public float[] PathData()
    {
        return mPathData;
    }

    public List<Point> getPoints()
    {
        return mPoints;
    }

    public List<Float> getPrs()
    {
        return mPrs;
    }

    public void UpdateData(HWCanvas canvas) {
        canvas.UpdateData(this);

        if(mPoints.size()>0) {
            Point pt = mPoints.get(mPoints.size() - 1);
            if(pt.x == StrokeCollection.ENDPOINT.x && pt.y == StrokeCollection.ENDPOINT.y) {
                mData.add(Float.valueOf(StrokeCollection.ENDPOINT.x));
                mData.add(Float.valueOf(StrokeCollection.ENDPOINT.y));
                mData.add(Float.valueOf(StrokeCollection.ENDPRESSURE));

                int size = mPoints.size();
                mPts = new int[size*2];
                mPressures = new float[size];
                mPathData = new float[mData.size()];

                int indexPoint = 0;

                for(int i = 0; i<mData.size(); i++)
                    mPathData[i] = mData.get(i);

                for(int i = 0; i< size; i++) {
                    mPts[indexPoint++] = mPoints.get(i).x;
                    mPts[indexPoint++] = mPoints.get(i).y;
                    mPressures[i] = mPrs.get(i);
                }
            }
        }
    }

    public void add(Point pt, float pressure) {
        mPoints.add(pt);
        mPrs.add(pressure);
    }

    public void addData(float[] data) {
        int size = (int)data[0];

        for(int i = 0; i<size; i++) {
            mData.add(data[i * 3 + 1]);
            mData.add(data[i * 3 + 2]);
            mData.add(data[i * 3 + 3]);
        }
    }

    public void updateBounds(int[] rect) {
        if(mBounds.width() == 0 && mBounds.height() == 0)
            mBounds.set(rect[0], rect[1], rect[2], rect[3]);
        else
            mBounds.union(rect[0], rect[1], rect[2], rect[3]);
    }

    public void updatePathData(float[] data, int[] rect) {
        int num = (int)data[0];
        if(num == 0)
            return;

        if(num >= 1000)
            num = 1000;

        mPathData = new float[num+3];
        System.arraycopy(data, 1, mPathData, 0, num);

        mPathData[num] = StrokeCollection.ENDPOINT.x;
        mPathData[num + 1] = StrokeCollection.ENDPOINT.y;
        mPathData[num + 2] = StrokeCollection.ENDPRESSURE;

        mBounds.set(rect[0], rect[1], rect[2], rect[3]);

        mIsInterpolate = false;
    }

    /**
     * 指示当使用指定的 HWPen 绘制此 HWPath 时，指定矩形是否与在后者的轮廓有交集
     * 说明：一般是在擦除过程中，判断是否擦除这条路径
     *
     * @param rc
     *            指定矩形   
     * @param pen
     *            指定HWPen
     *
     * @return    是否被擦除
     */
    public boolean IsOutlineVisible(Rect rc, HWPen pen) {
        if(!rc.intersect(mBounds))
            return false;

        if(mPoints.size() >= 2) {
            int penWidth = pen.getWidth();

            int index = 0;
            Rect rect = new Rect();

            int x =  mPoints.get(index).x;
            int y =  mPoints.get(index++).y;

            int x1 =  mPoints.get(index).x;
            int y1 =  mPoints.get(index++).y;
            rect.set(x, y, x1, y1);
            rect.sort();
            rect.inset(-penWidth, -penWidth);

            if(rc.intersect(rect))
                return true;

            while(x1 != StrokeCollection.ENDPOINT.x && y1 != StrokeCollection.ENDPOINT.y ) {
                x =  x1;
                y =  y1;

                x1 =  mPoints.get(index).x;
                y1 =  mPoints.get(index++).y;
                rect.set(x, y, x1, y1);
                rect.sort();
                rect.inset(-penWidth, -penWidth);

                if(rc.intersect(rect))
                    return true;
            }
        }

        return false;
    }
}
