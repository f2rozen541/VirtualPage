package com.hanvon.core;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * -------------------------------
 * Description:管理一页内所有的笔迹。
 * 提供addStroke，removeStroke，addPoint，erasePoint，clearBackground等方法。
 * -------------------------------
 */
public class StrokeCollection {

    //每一页中都有一个StrokeCollection对象来统一管理笔迹

    public static final int FINGER = 0;
    public static final int PEN = 1;
    public static final int ERASER = 2;

    public static final Point ENDPOINT = new Point(-100, -100);
    public static final float ENDPRESSURE = -100f;

    private HWCanvas mCanvas;
    private List<Stroke> mStrokes = new ArrayList<Stroke>();
    private Stroke current;

    private List<Point> mErasePts = new ArrayList<Point>();
    private Rect mRect = new Rect();
    private String TAG = "StrokeCollection";

    public StrokeCollection(int width, int height, int[] pixels, int[] backgroundPixels) {
        mCanvas = new HWCanvas(width, height, pixels, backgroundPixels);
        current = null;
    }

    public StrokeCollection(int width, int height, byte[] pixels, byte[] backgroundPixels) {
        mCanvas = new HWCanvas(width, height, pixels, backgroundPixels);
        current = null;
    }

    public StrokeCollection(int width, int height, int[] pixels, int color) {
        mCanvas = new HWCanvas(width, height, pixels, color);
        current = null;
    }

    public HWCanvas getCanvas() {
        return mCanvas;
    }

    public List<Stroke> getStrokes() {
        return mStrokes;
    }

    public Rect addStroke(Stroke stroke) {

        current = stroke;
        mCanvas.setPen(current.getHWPen());
        mStrokes.add(current);
        mCanvas.draw(stroke.getHWPath(), stroke.getHWPen(), false);
        return current.getBounds();
    }

    public void UpdateData() {
        if (current != null)
            current.UpdateData(mCanvas);
    }


    /**
     * 添加新的笔迹
     */
    public Rect addStroke(int penStyle, int penColor, int penWidth) {
        Rect rc = new Rect();
        if (current != null && current.endStroke())
            rc = endStroke(current.mDownTime);

        current = new Stroke(penStyle, penColor, penWidth);
        mCanvas.setPen(current.getHWPen());
        mStrokes.add(current);

        return rc;
    }

    /**
     * 添加新的笔迹：可以设置具体的颜色
     */
    public Rect addStroke(int penStyle, int penColor, int penWidth, boolean isColor) {
        Rect rc = new Rect();
        if (current != null && current.endStroke()) {
            rc = endStroke(current.mDownTime);
        }

        current = new Stroke(penStyle, penColor, penWidth, isColor);
        mCanvas.setPen(current.getHWPen());
        mStrokes.add(current);
        return rc;
    }

    public void setInputType(int inputType) {
        current.mInputType = inputType;
    }

    public void setDownTime(long downTime) {
        if (current != null) {
            current.mDownTime = downTime;
            current.mCreateTime = System.currentTimeMillis();
        }
    }

    public void setEndedTime(long downTime, long time) {
        if (current != null && current.mDownTime == downTime) {
            current.mEndTime = time;
        }
    }

    /**
     * 为当前笔迹添加新的点和压感值
     */
    public Rect add(Point pt, float pressure) {
        if (current != null) {
            return current.addPoint(mCanvas, pt, pressure);
        } else {
            return new Rect();
        }
    }


    public Rect add(Point pt, float pressure, long downTime) {
//		if(current != null && current.mInputType == downTime)
//			return current.addPoint(mCanvas, pt, pressure);
//		else
        {
            if (mStrokes != null && mStrokes.size() > 0) {
                Stroke cur = null;
                for (int i = mStrokes.size() - 1; i >= 0; i--) {
                    cur = mStrokes.get(i);
                    if (cur.mDownTime == downTime)
                        return cur.addPoint(mCanvas, pt, pressure);
                }
            }
            return new Rect();
        }
    }

    public Rect endStroke(long downTime) {
        Rect rc = new Rect();
        Stroke cur = null;
        if (mStrokes != null && mStrokes.size() > 0) {
            for (int i = mStrokes.size() - 1; i >= 0; i--) {
                cur = mStrokes.get(i);
                if (cur.mDownTime == downTime) {
                    rc = cur.addPoint(mCanvas, StrokeCollection.ENDPOINT, StrokeCollection.ENDPRESSURE);
                    break;
                }
            }
        }

        if (cur != null && cur.mDownTime == downTime) {
            long oldEndTime = cur.mEndTime;
            cur.mEndTime = System.currentTimeMillis();
            if (cur.mInputType == StrokeCollection.FINGER && oldEndTime > 0 &&
                    ((cur.getBounds().width() < 30 && cur.getBounds().height() < 30)
                            || (oldEndTime > cur.mEndTime && cur.mEndTime - cur.mCreateTime < 500
                            && cur.getBounds().width() < 40 && cur.getBounds().height() < 40))) {
                rc = removeStroke(cur);
            }
        }

        return rc;
    }

    /**
     * 擦除笔迹
     */
    public Rect erase(Point pt) {
        Rect rc = new Rect();

        int size = mErasePts.size();
        if (size == 0) {
            mErasePts.add(pt);
        } else {
            boolean isErase = false;
            Point last = mErasePts.get(mErasePts.size() - 1);

            if (pt.x == StrokeCollection.ENDPOINT.x && pt.y == StrokeCollection.ENDPOINT.y) {
                if (size == 1) {
                    mRect.set(last.x, last.y, last.x, last.y);
                    mRect.inset(-8, -8);
                    isErase = true;
                }

                mErasePts.clear();
            } else {
                isErase = true;
                mRect.set(last.x, last.y, pt.x, pt.y);
                mRect.sort();
                mErasePts.add(pt);
            }

            if (isErase) {
                if (mRect.width() > 0 || mRect.height() > 0) {
                    Stroke cur;
                    boolean isFirst = true;
                    //long first = System.currentTimeMillis();
                    mCanvas.setBackground();

                    for (int i = mStrokes.size() - 1; i >= 0; i--) {
                        cur = mStrokes.get(i);
                        if (cur.IsIn(mRect)) {
                            if (isFirst) {
                                rc.set(cur.getBounds());
                                isFirst = false;
                            } else
                                rc.union(cur.getBounds());

                            //mCanvas.addCilpRect(cur.getBounds());
                            cur.reDraw(mCanvas, true);

                            PageEditorActivity.mSelectedStrokeList.add(cur);//undo redo

                            mStrokes.remove(i);
                        }
                    }

                    //long second = System.currentTimeMillis();

                    boolean temp = rc.setIntersect(rc, new Rect(0, 0, mCanvas.getWidth(), mCanvas.getHeight()));

                    if (!isFirst) {
                        //mCanvas.clearBackground();
                        mCanvas.setCilpRect(rc);
                        Iterator<Stroke> it = mStrokes.iterator();
                        while (it.hasNext()) {
                            cur = it.next();
                            if (Rect.intersects(rc, cur.getBounds()))
                                cur.reDraw(mCanvas, false);
                        }

                        mCanvas.reSet();
                    }

                    //Log.d("redraw", String.valueOf(System.currentTimeMillis() - second));
                }
            }
        }

        return rc;
    }


    // removeId != 0  表示有多点触摸引起的，并且删除多点触控前笔迹
    // removeId = 0 表示要笔down的时候
    public Rect removeStroke(long removeId) {
        Stroke cur;
        Rect rc = new Rect();
        Rect rc2 = new Rect();
        boolean isErase = false;
        boolean isFirst = true;

        //保险起见
        if (current != null && current.endStroke())
            rc2 = endStroke(current.mDownTime);

        long creatTime = 0;

        if (removeId != 0) {
            if (current != null && current.mDownTime == removeId)
                creatTime = current.mCreateTime;
            else if (mStrokes != null && mStrokes.size() > 0) {
                cur = null;
                for (int i = mStrokes.size() - 1; i >= 0; i--) {
                    cur = mStrokes.get(i);
                    if (cur.mDownTime == removeId) {
                        creatTime = current.mCreateTime;
                        break;
                    }
                }
            }
        } else
            creatTime = System.currentTimeMillis();

        for (int i = mStrokes.size() - 1; i >= 0; i--) {
            cur = mStrokes.get(i);
            if (cur.mInputType != StrokeCollection.FINGER || cur.mDownTime == 0) //cur.mDownTime == 0 表示由存储后打开的
                continue;

            boolean isRemove = false;
            if (cur.mDownTime == removeId //多点触控生成的笔迹
                    || (creatTime - cur.mEndTime > 0 && creatTime - cur.mEndTime < 600
                    && creatTime - cur.mCreateTime > 0 && creatTime - cur.mCreateTime < 1000
                    && cur.mEndTime - cur.mCreateTime < 500
                    && cur.getBounds().width() < 30
                    && cur.getBounds().height() < 30)) //多点触控前笔迹 或者 笔down的时候
                isRemove = true;

            if (isRemove) {
                mCanvas.setBackground();
                if (isFirst) {
                    rc.set(cur.getBounds());
                    isFirst = false;
                } else
                    rc.union(cur.getBounds());

                cur.reDraw(mCanvas, true);
                mStrokes.remove(i);

                isErase = true;
                if (cur.equals(current))
                    current = null;
            }
        }

        if (isErase) {
            mCanvas.setCilpRect(rc);
            Iterator<Stroke> it = mStrokes.iterator();
            while (it.hasNext()) {
                cur = it.next();
                if (Rect.intersects(rc, cur.getBounds()))
                    cur.reDraw(mCanvas, false);
            }

            mCanvas.reSet();
        }

        if (rc2.width() != 0 && rc2.height() != 0)
            rc.union(rc2);

        return rc;
    }

    public Rect removeStroke(Stroke stroke) {
        Rect rc = new Rect();

        if (mStrokes != null && mStrokes.size() > 0) {
            Stroke cur = null;
            for (int i = mStrokes.size() - 1; i >= 0; i--) {
                cur = mStrokes.get(i);
                if (cur.equals(stroke)) {
                    rc = cur.getBounds();

                    mCanvas.setBackground();

                    cur.reDraw(mCanvas, true);
                    mStrokes.remove(i);

                    if (cur.equals(current))
                        current = null;

                    break;
                }
            }

            if (rc.width() != 0 && rc.height() != 0) {
                mCanvas.setCilpRect(rc);
                Iterator<Stroke> it = mStrokes.iterator();
                while (it.hasNext()) {
                    cur = it.next();
                    if (Rect.intersects(rc, cur.getBounds()))
                        cur.reDraw(mCanvas, false);
                }

                mCanvas.reSet();
            }
        }

        return rc;
    }

    public Rect reDraw() {
        Rect rc = new Rect();
        rc.set(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
        //mCanvas.clearBackground();
        mCanvas.setCilpRect(rc);
        Iterator<Stroke> it = mStrokes.iterator();
        while (it.hasNext()) {
            Stroke cur = it.next();
            //if(Rect.intersects(rc, cur.getBounds()))
            cur.reDraw(mCanvas, false);
        }

        mCanvas.reSet();

        return rc;
    }

    /**
     * 清空所有笔迹
     */
    public void clearBackground() {
        if (mStrokes != null) {
            mStrokes.clear();
        }
        if (mCanvas != null) {
            mCanvas.clear();
        }
    }

    /**
     * 换背景
     *
     * @param width            宽度
     * @param height           高度
     * @param pixels           当前操作的像素值数组
     * @param backgroundPixels 所换背景像素值数组
     */
    public void clearBackground(int width, int height, int[] pixels, int[] backgroundPixels) {
        mCanvas.attach(width, height, pixels, backgroundPixels);
        Iterator<Stroke> it = mStrokes.iterator();
        //设置刷新区域test
        Rect rc = new Rect();
        rc.set(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
        Log.d(TAG, "clearBackground() called with: " + "width = [" + width + "], height = [" + height + "],mCanvas.getWidth():" + mCanvas.getWidth() + "mCanvas.getHeight()" + mCanvas.getHeight());
        //mCanvas.clearBackground();
        mCanvas.setCilpRect(rc);

        Stroke cur = null;
        while (it.hasNext()) {
            cur = it.next();
            cur.reDraw(mCanvas, false);
        }
    }

    public void clearBackground(int width, int height, byte[] pixels, byte[] backgroundPixels) {
        mCanvas.attach(width, height, pixels, backgroundPixels);
        Iterator<Stroke> it = mStrokes.iterator();

        Stroke cur = null;
        while (it.hasNext()) {
            cur = it.next();
            cur.reDraw(mCanvas, false);
        }
    }
}
