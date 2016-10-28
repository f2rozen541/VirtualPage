package com.hanvon.virtualpage.pageeditor.tools.stroke;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;

import com.hanvon.core.Stroke;
import com.hanvon.core.StrokeCollection;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.PageRotation;
import com.hanvon.virtualpage.common.ConstantValues;
import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeStyleType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeWidthType;
import com.hanvon.virtualpage.utils.LogUtil;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * -------------------------------
 * Description:输入笔迹，
 * 并完成对笔迹的擦除，复制，删除，剪切，粘贴，撤销，恢复，缩放等操作。
 * -------------------------------
 */
public class StrokeView extends View implements View.OnClickListener {
    private Bitmap mStrokeBitmap;
    private int[] mStrokeViewPixels;
    private int[] mBackgroundPixels;
    private int sWidth;
    private int sHeight;

    private int mCurrentColor = Color.BLACK;
    private int hsValueR = 0;
    private int hsValueG = 0;
    private int hsValueB = 0;
    private StrokeStyleType mCurrentStyleType = StrokeStyleType.PEN;
    private StrokeWidthType mCurrentWidthType = StrokeWidthType.MEDIUM;
    private boolean mWriteMode = true;
//    private boolean mOnLayoutFlag = false;
    private int mWidth;
    private int mHeight;
    public StrokeCollection mStrokeCollection;
    public PageRotation mPageRotation;

    private boolean mIsUsedFinger = true;

    private Method mDisable_vsync = null;
    private Method mEnable_vsync = null;

    private long mRemoveId = 0;
    private long mLastPenUpTime;
    private long mMultiTouchUpTime;

    InputMethodManager imm;

    // 记录本次倍率
    public static float rate = 1.0f;
    // 记录上次的倍率
    private float oldRate = 1.0f;
    // 最小缩放倍率
    private static float mMinRate = 0.9f;
    // 最大缩放倍率
    private static float mMaxRate = 4.0f;
    // 记录上次触屏时线段的距离
    private float oldLineDistance = 0.1f;
    // 是否按下
    private boolean mIsDown = false;
    // 是否第一次多指触点屏幕
    private boolean isFirst = true;
    // 是否双指
    private boolean isDoubleFinger = false;
    //缩放的中点
    private PointF midPoint = new PointF(0, 0);
    //旧的缩放的中点
    private PointF mOldMidPoint = new PointF(0, 0);
    //双指时，第一个手指落下的时间
    private long mFirstFingerDownTime = 0;
    //双指落下的时间差
    private long mIntervalTime = 0;
    //双指落下最大时间差，超过这个时间认为不是缩放。（双指缩放时单指操作失效，单指操作时，双指缩放失效）
    private long mMaxIntervalTime = 200;
    //用于计算双指移动距离的第一点
    private PointF mOldMovePoint = new PointF(0, 0);
    //用于计算双指移动距离的第二点
    private PointF mNewMovePoint = new PointF(0, 0);
    //双指移动x距离
    private float mTranslateX = 0;
    //双指移动Y距离
    private float mTranslateY = 0;
    //上一次双指移动x距离
    private float mOldTranslateX = 0;
    //上一次双指移动Y距离
    private float mOldTranslateY = 0;

    // Start: by wangkun20 to fix YETIM-5108
    // 画笔从工具栏到绘图区域移动时的临界时刻
    private long startTime = 0;
    // 画笔从工具栏到绘图区域，再返回工具栏，中间停留在中间绘图区域的时长
    private long intervalTime = 0;
    // intervalTime小于delayTime，工具栏状态一致保持为隐藏状态，超出延时时长则工具栏显示
    private long delayTime = 1000;
    // End: by wangkun20 to fix YETIM-5108

    private Paint mCornerPaint;
    private Paint mBorderPaint;

    private float selectStartX;
    private float selectStartY;
    private float selectEndX;
    private float selectEndY;

    private boolean isPaint = false;
    private boolean isClear = false;
    private int mClickState = -1;
    private int mSelectMode = 0;

    private Path pathRect = new Path();

    private PopupWindow mpopupWindow;
    private PopupWindow mPastePopupWindow;
    public boolean isCanPaste = false;
    private Point mPasteStartPoint = new Point(0, 0);

    private Context mContext;

    private ZoomCanvasListener mZoomCanvasListener = null;
    private SelectedRectListener mSelectedRectListener = null;
    private HideToolBarListener mHideToolBarListener = null;
    private StartTouchStrokeViewListener mStrokeTouchStrokeViewListener = null;
    private ClearFocusListener mClearFocusListener = null;

    protected int getBitmapWidth() {
        return mWidth;
    }

    protected int getBitmapHeight() {
        return mHeight;
    }

    public Point getPasteStartPoint() {
        return mPasteStartPoint;
    }

    public StrokeView(Context context) {
        super(context);
        Logger.d("StrokeView(Context context)");
        mContext = context.getApplicationContext();
        getDeclaredMethod();
        init();
    }

    public StrokeView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mContext = context;
        Logger.i("StrokeView(Context context, AttributeSet attrs)");
        mContext = context.getApplicationContext();
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
        getDeclaredMethod();
        init();
    }

    public interface ZoomCanvasListener {
        void displayZoomWindow(boolean isDisplay, Bitmap screenBitmap, float rate, float translateX, float translateY);
    }

    public void setZoomCanvasListener(ZoomCanvasListener zoomCanvasListener) {
        mZoomCanvasListener = zoomCanvasListener;
    }

    public interface SelectedRectListener {
        void copySelectedRect();

        void cutSelectedRect();

        void deleteSelectedRect();

        void pasteSelectedRect();
    }

    public void setSelectedRectListener(SelectedRectListener selectedRectListener) {
        mSelectedRectListener = selectedRectListener;
    }

    public interface HideToolBarListener {
        void hideTopToolBar(boolean hideFlag);

        void hideBottomToolBar(boolean hideFlag);
    }

    public void setHideToolBarListener(HideToolBarListener hideToolBarListener) {
        mHideToolBarListener = hideToolBarListener;
    }

    public interface StartTouchStrokeViewListener {
        boolean onStartTouchStrokeView();
    }

    public void setOnStartTouchStrokeViewListener(StartTouchStrokeViewListener startTouchStrokeViewListener) {
        mStrokeTouchStrokeViewListener = startTouchStrokeViewListener;
    }

    public interface ClearFocusListener {
        void clearFocusListener();
    }

    public void setClearFocusListener(ClearFocusListener clearFocusListener) {
        mClearFocusListener = clearFocusListener;
    }

    private void init() {
        mBorderPaint = new Paint();
        mBorderPaint.setColor(0xFF000000);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setDither(true);
        mBorderPaint.setStrokeWidth(1);
        mBorderPaint.setPathEffect(new DashPathEffect(new float[]{3, 6}, 0));

        mCornerPaint = new Paint();
        mCornerPaint.setColor(0xFFFF9900);
        mCornerPaint.setAntiAlias(true);
        mCornerPaint.setStyle(Paint.Style.FILL);
        mCornerPaint.setDither(true);

    }

    public void setSelectMode(int mode) {
        mSelectMode = mode;
    }


    @Override
    public void onClick(View v) {
        deletePopwindows();
        switch (v.getId()) {
            case R.id.bt_copy:
                if (mSelectedRectListener != null) {
                    mSelectedRectListener.copySelectedRect();
                }
                isCanPaste = true;
                break;

            case R.id.bt_delete:
                if (mSelectedRectListener != null) {
                    mSelectedRectListener.deleteSelectedRect();
                }
                break;

            case R.id.bt_cut:
                if (mSelectedRectListener != null) {
                    mSelectedRectListener.cutSelectedRect();
                }
                isCanPaste = true;
                break;
            case R.id.bt_paste:
                if (mSelectedRectListener != null) {
                    mSelectedRectListener.pasteSelectedRect();
                }
                isCanPaste = false;
                break;
            case R.id.bt_cancel:
                isCanPaste = false;
                break;
            default:
                break;
        }
    }

    private void showPopMenu(RectF rect) {
        View view = View.inflate(mContext, R.layout.edit_popup_menu, null);

        Button bt_copy = (Button) view.findViewById(R.id.bt_copy);
        Button bt_cut = (Button) view.findViewById(R.id.bt_cut);
        Button bt_delete = (Button) view.findViewById(R.id.bt_delete);

        bt_copy.setOnClickListener(this);
        bt_cut.setOnClickListener(this);
        bt_delete.setOnClickListener(this);

        if (mpopupWindow == null) {
            mpopupWindow = new PopupWindow(this);
            mpopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
            mpopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            mpopupWindow.setBackgroundDrawable(new BitmapDrawable());
            mpopupWindow.setFocusable(false);
        }

        mpopupWindow.setContentView(view);

        if (PageEditorActivity.canvasRotation == 0) {
            mpopupWindow.showAtLocation(this, Gravity.LEFT | Gravity.TOP, (int) rect.right - 450, (int) rect.bottom + 10);
        } else if (PageEditorActivity.canvasRotation == 90) {
            mpopupWindow.showAtLocation(this, Gravity.RIGHT | Gravity.TOP, (int) rect.top, (int) rect.right + 10);
        } else if (PageEditorActivity.canvasRotation == 180) {
            mpopupWindow.showAtLocation(this, Gravity.RIGHT | Gravity.BOTTOM, (int) rect.left, (int) rect.top - 95);
        } else if (PageEditorActivity.canvasRotation == 270) {
            mpopupWindow.showAtLocation(this, Gravity.LEFT | Gravity.BOTTOM, (int) rect.bottom - 450, (int) rect.left - 95);
        }
        mpopupWindow.update();
    }

    private void showPastePopMenu(int x, int y) {
        View view = View.inflate(mContext, R.layout.edit_paste_popup_menu, null);

        Button bt_paste = (Button) view.findViewById(R.id.bt_paste);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        bt_paste.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);

        if (mPastePopupWindow == null) {
            mPastePopupWindow = new PopupWindow(this);
            mPastePopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
            mPastePopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
            mPastePopupWindow.setBackgroundDrawable(new BitmapDrawable());

            mPastePopupWindow.setFocusable(false);
            mPastePopupWindow.setOutsideTouchable(true);
        }

        mPastePopupWindow.setContentView(view);

        if (PageEditorActivity.canvasRotation == 0) {
            mPastePopupWindow.showAtLocation(this, Gravity.LEFT | Gravity.TOP, x, y);
        } else if (PageEditorActivity.canvasRotation == 90) {
            mPastePopupWindow.showAtLocation(this, Gravity.RIGHT | Gravity.TOP, y, x);
        } else if (PageEditorActivity.canvasRotation == 180) {
            mPastePopupWindow.showAtLocation(this, Gravity.RIGHT | Gravity.BOTTOM, x, y);
        } else if (PageEditorActivity.canvasRotation == 270) {
            mPastePopupWindow.showAtLocation(this, Gravity.LEFT | Gravity.BOTTOM, y, x);
        }
        mPastePopupWindow.update();
    }

    private void getDeclaredMethod() {
        if (mDisable_vsync != null && mDisable_vsync != null) {
            return;
        }

        try {
            Class c = Class.forName("android.hwpen.HW_pen");
            try {
                mDisable_vsync = c.getDeclaredMethod("disable_vsync", StrokeView.class);
            } catch (Exception e) {
                mDisable_vsync = null;
            }

            if (mDisable_vsync != null) {
                try {
                    mEnable_vsync = c.getDeclaredMethod("enable_vsync", StrokeView.class);
                } catch (Exception e) {
                    mEnable_vsync = null;
                    mDisable_vsync = null;
                }
            }
        } catch (ClassNotFoundException e) {
            mEnable_vsync = null;
            mDisable_vsync = null;
        }
    }

    private void initBackgroundBitmap() {
        fillBackground(mStrokeBitmap); // 调用的子类NoteStrokeView中的方法
//        System.gc();

        for (int index = 0; index < mStrokeViewPixels.length; index++) {
            mStrokeViewPixels[index] = 0;
        }
        for (int index = 0; index < mBackgroundPixels.length; index++) {
            mBackgroundPixels[index] = 0;
        }
        mStrokeBitmap.getPixels(mStrokeViewPixels, 0, mWidth, 0, 0, mWidth, mHeight);
        mStrokeBitmap.getPixels(mBackgroundPixels, 0, mWidth, 0, 0, mWidth, mHeight);

        if (mStrokeCollection == null) {
            mStrokeCollection = new StrokeCollection(mWidth, mHeight, mStrokeViewPixels, mBackgroundPixels);
        } else {
            mStrokeCollection.clearBackground(mWidth, mHeight, mStrokeViewPixels, mBackgroundPixels);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
//            int width = right - left;
//            int height = bottom - top;
//            if (height > width) {
//                mWidth = width;
//                mHeight = height;
//            } else {
//                mWidth = width;
//                mHeight = height;
//            }
            mWidth = right - left;
            mHeight = bottom - top;
            sWidth = mWidth;
            sHeight = mHeight;
            if (sWidth <= 0 || sHeight <= 0) {
                sWidth = 1920;
                sHeight = 1200;
            }
//            if (mStrokeViewPixels == null) {
//                mStrokeViewPixels = new int[sWidth * sHeight];
//            }
//            if (mBackgroundPixels == null) {
//                mBackgroundPixels = new int[sWidth * sHeight];
//            }
//            if (mStrokeBitmap != null) {
//                freeBitmap();
//            }
//            if (sWidth != mWidth || sHeight != mHeight) {
//                sWidth = mWidth;
//                sHeight = mHeight;
//            }
            mStrokeViewPixels = ConstantValues.getInstance().getStrokeViewPixels();
            mBackgroundPixels = ConstantValues.getInstance().getStrokeViewBgPixels();
            mStrokeBitmap = Bitmap.createBitmap(sWidth, sHeight, Bitmap.Config.ARGB_8888);

            initBackgroundBitmap();

//            if (!mOnLayoutFlag) {
//                mOnLayoutFlag = true;
//            }
            invalidate(); // 通知刷新View
        }
    }

    /**
     * fill background to bitmap,derived class should overwrite this method to
     * fill background to bitmap by its own way.
     *
     * @param bitmap
     */
    protected void fillBackground(Bitmap bitmap) {
        bitmap.eraseColor(Color.WHITE);
    }

    /**
     * width and height of the bitmap has changed on method
     */
    public void changeBackGroundBitmap() {
        if (mStrokeBitmap != null) {
            fillBackground(mStrokeBitmap);
            mStrokeBitmap.getPixels(mStrokeViewPixels, 0, mWidth, 0, 0, mWidth, mHeight);
            mStrokeBitmap.getPixels(mBackgroundPixels, 0, mWidth, 0, 0, mWidth, mHeight);
            try {
                mStrokeCollection.clearBackground(mWidth, mHeight, mStrokeViewPixels, mBackgroundPixels);
            } catch (Exception ex) {
            }
            mStrokeBitmap.setPixels(mStrokeViewPixels, 0, mWidth, 0, 0, mWidth, mHeight);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (mSelectMode == 0) {

            if (!mIsDown) {//画布被缩小时，双指松开，画布变为100%显示，且不平移
                if (rate < 1) {
                    rate = 1;
                    oldRate = 1;
                    mTranslateX = 0;
                    mTranslateY = 0;
                    mOldTranslateX = 0;
                    mOldTranslateY = 0;
                }
            }

            //canvas.scale(rate, rate, midPoint.x, midPoint.y);//两指中心
            canvas.scale(rate, rate, 1920 / 2, 1200 / 2);//zoom
            canvas.translate(mTranslateX, mTranslateY);//move

            if (mStrokeBitmap != null && !mStrokeBitmap.isRecycled()) {
                canvas.drawBitmap(mStrokeBitmap, 0, 0, null);
            }

            EditorState.getInstance().setCanvasRate(rate);
            EditorState.getInstance().setCanvasTranslateX(mTranslateX);
            EditorState.getInstance().setCanvasTranslateY(mTranslateY);
        } else {
            super.onDraw(canvas);

            if (mStrokeBitmap != null && !mStrokeBitmap.isRecycled()) {
                canvas.drawBitmap(mStrokeBitmap, 0, 0, null);
            }

            if (mClickState == 1) {
                if (isPaint) {
                    RectF rect = new RectF(Math.min(selectStartX, selectEndX), Math.min(selectStartY, selectEndY),
                            Math.max(selectStartX, selectEndX), Math.max(selectStartY, selectEndY));

                    if (rect.width() > 10 && rect.height() > 10) {
                        pathRect.moveTo(rect.left, rect.top);
                        pathRect.lineTo(rect.right, rect.top);
                        pathRect.lineTo(rect.right, rect.bottom);
                        pathRect.lineTo(rect.left, rect.bottom);
                        pathRect.close();
                        canvas.drawPath(pathRect, mBorderPaint);
                        pathRect.reset();

                        canvas.drawCircle(rect.left, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.left, rect.bottom, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, rect.bottom, 8, mCornerPaint);
                        canvas.drawCircle((rect.left + rect.right) / 2, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, (rect.top + rect.bottom) / 2, 8, mCornerPaint);
                        canvas.drawCircle(rect.left, (rect.top + rect.bottom) / 2, 8, mCornerPaint);
                        canvas.drawCircle((rect.left + rect.right) / 2, rect.bottom, 8, mCornerPaint);
                    }
                }
            } else if (mClickState == 2) {
                if (isPaint) {
                    RectF rect = new RectF(Math.min(selectStartX, selectEndX), Math.min(selectStartY, selectEndY),
                            Math.max(selectStartX, selectEndX), Math.max(selectStartY, selectEndY));

                    if (rect.width() > 10 && rect.height() > 10) {
                        canvas.drawRect(rect, mBorderPaint);

                        canvas.drawCircle(rect.left, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.left, rect.bottom, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, rect.bottom, 8, mCornerPaint);
                        canvas.drawCircle((rect.left + rect.right) / 2, rect.top, 8, mCornerPaint);
                        canvas.drawCircle(rect.right, (rect.top + rect.bottom) / 2, 8, mCornerPaint);
                        canvas.drawCircle(rect.left, (rect.top + rect.bottom) / 2, 8, mCornerPaint);
                        canvas.drawCircle((rect.left + rect.right) / 2, rect.bottom, 8, mCornerPaint);

                        showPopMenu(rect);
                    }
                }

                mClickState = -1;
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //浏览List消失
        if (mStrokeTouchStrokeViewListener != null) {
            if (mStrokeTouchStrokeViewListener.onStartTouchStrokeView()) {
                return false;
            }
        }

        //画矩形选择框
        if (mSelectMode == 1) {
            deletePopwindows();

            if (isCanPaste) {
                mPasteStartPoint.x = (int) event.getX();
                mPasteStartPoint.y = (int) event.getY();
                showPastePopMenu((int) event.getX(), (int) event.getY());
                return true;
            }
            isPaint = true;
            isClear = false;

            int x = (int) event.getX();
            int y = (int) event.getY();


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mClearFocusListener != null) {
                        mClearFocusListener.clearFocusListener();
                    }
                    selectTouchBegan(x, y);
                    break;

                case MotionEvent.ACTION_MOVE:
                    selectTouchMoved(x, y);
                    invalidate();
                    break;

                case MotionEvent.ACTION_UP:
                    selectTouchEnded(x, y);
                    invalidate();
                    break;
            }
            return true;
        }


        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getToolType(0) == MotionEvent.TOOL_TYPE_ERASER) {
                return false;
            }
        }

        if (!mIsUsedFinger && event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {
            return true;
        }

        float pressure = 0.8f;
        int x = (int) event.getX();
        int y = (int) event.getY();

        Rect refreshRect;

        if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
            pressure = event.getPressure();
        }

        /*if (handleZoom(event)) {//处理双指缩放
            return true;
        }*/

        if (handleTwoFingers(event)) {//处理双指操作
            return true;
        }

        if (mWriteMode) {
//            if (event.getAction() > 2) {
//                mRemoveId = event.getDownTime();
//                refreshRect = mStrokeCollection.removeStroke(event
//                        .getDownTime());
//                if (refreshRect.width() != 0 && refreshRect.height() != 0) {
//                    mStrokeBitmap.setPixels(mStrokeViewPixels,
//                            refreshRect.top * mWidth + refreshRect.left, mWidth,
//                            refreshRect.left,
//                            refreshRect.top,
//                            refreshRect.width(),
//                            refreshRect.height());
//                    invalidate(refreshRect);
//                }
//                return true;
//            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //滑动弹出工具栏，不画出笔迹
                    if ((PageEditorActivity.canvasRotation == 0 && y > 1190)
                            || (PageEditorActivity.canvasRotation == 90 && x > 1910)
                            || (PageEditorActivity.canvasRotation == 180 && y < 10)
                            || (PageEditorActivity.canvasRotation == 270 && x < 10)
                            || (PageEditorActivity.canvasRotation == 0 && y < 10)
                            || (PageEditorActivity.canvasRotation == 90 && x < 10)
                            || (PageEditorActivity.canvasRotation == 180 && y > 1190)
                            || (PageEditorActivity.canvasRotation == 270 && x > 1910)){
                        return true;
                    }

                    if (mClearFocusListener != null) {
                        mClearFocusListener.clearFocusListener();
                    }

                    mFirstFingerDownTime = event.getDownTime();
                    if (isDoubleFinger) {//双指操作时，单指失效
                        return true;
                    }
                    if (rate != 1 || mTranslateX != 0 || mTranslateY != 0) {//画布被缩放或者被平移 不可输入笔迹
                        return true;
                    }
                    //隐藏上下工具栏
                    hideToolBars(x, y);
                    if (!mIsDown) {
                        mFirstFingerDownTime = event.getDownTime();
                        mIsDown = true;
                    }

                    if (mDisable_vsync != null) {
                        try {
//                            mDisable_vsync.invoke(null, null); // 消除警告做的修改 2016年3月2日 10:58:45 by taozhi
                            mDisable_vsync.invoke(null, new Object[]{});
                        } catch (Exception e) {
                            mDisable_vsync = null;
                            mEnable_vsync = null;
                        }
                    }

                    long endTime = 0;
                    if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {

                    } else if (event.getToolType(0) == MotionEvent.TOOL_TYPE_FINGER) {
                        long now = System.currentTimeMillis();
                        if (now - mLastPenUpTime < 800)
                            endTime = mLastPenUpTime + 1200;
                        if (now - mMultiTouchUpTime < 800)
                            endTime = Math.max(endTime, mMultiTouchUpTime + 1200);
                    }

                    if (mCurrentStyleType.getmValue() == 5) {
                        hsValueR = (mCurrentColor >> 16) & 0xff;
                        hsValueG = (mCurrentColor >> 8) & 0xff;
                        hsValueB = mCurrentColor & 0xff;
                        mCurrentColor = Color
                                .argb(70, hsValueR, hsValueG, hsValueB);
                    }

                    //初次：new Stroke()，设定画笔属性。refreshRect（0，0，0，0）
                    //begin by cuishuo1
                    //当第一次手写时获取平板方向，并设置为该纸张的正方向
                    if (getStrokeList() != null && getStrokeList().size() == 0) {
                        int rotation = BaseApplication.getWindowManager().getDefaultDisplay().getRotation();
                        LogUtil.i("set current page rotation = "+rotation);

                        switch (rotation) {
                            case Surface.ROTATION_0:
                                mPageRotation = new PageRotation(Surface.ROTATION_0, Surface.ROTATION_180, false);
                                break;
                            case Surface.ROTATION_90:
                                mPageRotation = new PageRotation(Surface.ROTATION_90, Surface.ROTATION_270, false);
                                break;
                            case Surface.ROTATION_180:
                                mPageRotation = new PageRotation(Surface.ROTATION_180, Surface.ROTATION_0, false);
                                break;
                            case Surface.ROTATION_270:
                                mPageRotation = new PageRotation(Surface.ROTATION_270, Surface.ROTATION_90, false);
                                break;
                        }
                        if (PageEditorActivity.getInstance() != null) {
                            PageEditorActivity.getInstance().setPageRotation(mPageRotation);
                        }
                    }
                    //end by cuishuo1
                    refreshRect = mStrokeCollection.addStroke(
                            mCurrentStyleType.getmValue(), mCurrentColor,
                            mCurrentWidthType.getmValue(), true);

                    if (refreshRect.width() != 0 && refreshRect.height() != 0) {
                        mStrokeBitmap.setPixels(mStrokeViewPixels,
                                refreshRect.top * mWidth + refreshRect.left,
                                mWidth, refreshRect.left,
                                refreshRect.top,
                                refreshRect.width(),
                                refreshRect.height());
                        invalidate(refreshRect);
                    }

                    mStrokeCollection.setInputType(event.getMetaState());
                    mStrokeCollection.setDownTime(event.getDownTime());
                    mStrokeCollection.setEndedTime(event.getDownTime(), endTime);

                    //current stroke add Point(x, y)
                    //canvas draw Point(x, y),refreshRect(x-6,y-6,x+6,y+6)
                    refreshRect = mStrokeCollection.add(new Point(x, y), pressure, event.getDownTime());
                    if (refreshRect.width() != 0 && refreshRect.height() != 0) {
                        mStrokeBitmap.setPixels(mStrokeViewPixels,
                                refreshRect.top * mWidth + refreshRect.left, mWidth,
                                refreshRect.left,
                                refreshRect.top,
                                refreshRect.width(),
                                refreshRect.height());
                        invalidate(refreshRect);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isDoubleFinger) {//双指操作时，单指失效
                        return true;
                    }

                    if (rate != 1 || mTranslateX != 0 || mTranslateY != 0) {//画布被缩放或者被平移 不可输入笔迹
                        return true;
                    }

                    //隐藏上下工具栏
                    hideToolBars(x, y);

                    //current stroke add Point(x, y)
                    //canvas draw Point(x, y),refreshRect(x-6,y-6,x+6,y+6)
                    refreshRect = mStrokeCollection.add(new Point(x, y), pressure, event.getDownTime());
                    if (refreshRect.width() != 0 && refreshRect.height() != 0) {
                        mStrokeBitmap.setPixels(mStrokeViewPixels,
                                refreshRect.top * mWidth + refreshRect.left,
                                mWidth,
                                refreshRect.left,
                                refreshRect.top,
                                refreshRect.width(),
                                refreshRect.height());
                        invalidate(refreshRect);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (isDoubleFinger) {//抬起，双指操作动作结束，单指有效
                        isDoubleFinger = false;
                    }

                    if (mIsDown) {
                        mIsDown = false;
                    }

                    //显示上下工具栏
                    if (mHideToolBarListener != null) {
                        mHideToolBarListener.hideTopToolBar(false);
                        mHideToolBarListener.hideBottomToolBar(false);
                    }

                    int beforeSize;
                    if (getStrokeList() != null) {
                        beforeSize = getStrokeList().size();
                    } else {
                        beforeSize = 0;
                    }


                    //current stroke add Point(-100, -100)
                    refreshRect = mStrokeCollection.endStroke(event.getDownTime());
                    if (refreshRect.width() != 0 && refreshRect.height() != 0) {
                        mStrokeBitmap.setPixels(mStrokeViewPixels,
                                refreshRect.top * mWidth + refreshRect.left, mWidth,
                                refreshRect.left,
                                refreshRect.top,
                                refreshRect.width(),
                                refreshRect.height());
                        invalidate(refreshRect);

                        int afterSize = getStrokeList().size();

                        try {
                            if (getStrokeList().size() > 0 && afterSize == beforeSize) {
//                                PageEditorActivity.addRecord(getStrokeList().getLast());
                                if (PageEditorActivity.getInstance() != null) {
                                    PageEditorActivity.getInstance().addRecord(getStrokeList().getLast());
                                }
                            }
                        } catch (Exception e) {
                            LogUtil.e("添加记录出现异常：" + e.toString());
                        }
                    }

                    if (mZoomCanvasListener != null) {
                        mZoomCanvasListener.displayZoomWindow(false, mStrokeBitmap, rate, mTranslateX, mTranslateY);
                    }

                    if (event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS) {
                        mLastPenUpTime = System.currentTimeMillis();
                    }
                    if (event.getDownTime() == mRemoveId) {
                        mMultiTouchUpTime = System.currentTimeMillis();
                    }

                    if (mEnable_vsync != null) {

                        try {
//                            mEnable_vsync.invoke(null, null); // 消除警告做的修改 2016年3月2日 10:59:15 by taozhi
                            mEnable_vsync.invoke(null, new Object[]{});
                        } catch (Exception e) {
                            mDisable_vsync = null;
                            mEnable_vsync = null;
                        }
                    }

                    break;
            }
            return true;
        } else {
            return false;
        }
    }


    private void selectTouchBegan(float x, float y) {
        selectStartX = x;
        selectStartY = y;
        selectEndX = x;
        selectEndY = y;

        mClickState = 0;
    }


    private void selectTouchMoved(float x, float y) {
        selectEndX = x;
        selectEndY = y;

        mClickState = 1;
    }


    private void selectTouchEnded(float x, float y) {
        mClickState = 2;
    }

    public void deletePopwindows() {
        invalidate();
        if (mpopupWindow != null) {
            mpopupWindow.dismiss();
        }
        if (mPastePopupWindow != null) {
            mPastePopupWindow.dismiss();
        }
    }

    //隐藏工具栏
    // Start: modified by wangkun20 to fix YETIM-5108
    // add intervalTime > delayTime judgement
    private void hideToolBars(int x, int y) {
        if (PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 0
                || PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 0) {
            if (y < PageEditorActivity.topToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideTopToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideTopToolBar(false);
                }
            }

            if (y > PageEditorActivity.screenHeight - PageEditorActivity.bottomToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideBottomToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideBottomToolBar(false);
                }
            }
        } else if (PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 90
                || PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 90) {
            if (x < PageEditorActivity.topToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideTopToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideTopToolBar(false);
                }
            }

            if (x > PageEditorActivity.screenHeight - PageEditorActivity.bottomToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideBottomToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideBottomToolBar(false);
                }
            }
        } else if (PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 180
                || PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 180) {
            if (y < PageEditorActivity.bottomToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideBottomToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideBottomToolBar(false);
                }
            }

            if (y > PageEditorActivity.screenHeight - PageEditorActivity.topToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideTopToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideTopToolBar(false);
                }
            }
        } else if (PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 270
                || PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 270) {
            if (x < PageEditorActivity.bottomToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideBottomToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideBottomToolBar(false);
                }
            }

            if (x > PageEditorActivity.screenHeight - PageEditorActivity.topToolBarHeight) {
                if (mHideToolBarListener != null) {
                    mHideToolBarListener.hideTopToolBar(true);
                    startTime = System.currentTimeMillis();
                }
            } else {
                if (mHideToolBarListener != null) {
                    intervalTime = System.currentTimeMillis() - startTime;
                    if (intervalTime > delayTime)
                        mHideToolBarListener.hideTopToolBar(false);
                }
            }
        }
    }
    // End: modified by wangkun20 to fix YETIM-5108

    public void undo(Stroke undo) {
        Rect rect = mStrokeCollection.removeStroke(undo);
        if (rect.width() != 0 && rect.height() != 0) {
            mStrokeBitmap.setPixels(mStrokeViewPixels,
                    rect.top * mWidth + rect.left, mWidth,
                    rect.left,
                    rect.top,
                    rect.width(),
                    rect.height());
            invalidate(rect);
        }
    }

    public void undo(Object action, Stroke undo) {
        if (action.equals(2)) {//cut
            redo(undo);
        }
        if (action.equals(3)) {//delete
            redo(undo);
        }
        if (action.equals(4)) {//paste
            undo(undo);
        }
    }

    public void redo(Stroke redo) {
        Rect rect = mStrokeCollection.addStroke(redo);
        if (rect.width() != 0 && rect.height() != 0) {
            mStrokeBitmap.setPixels(mStrokeViewPixels,
                    rect.top * mWidth + rect.left, mWidth,
                    rect.left,
                    rect.top,
                    rect.width(),
                    rect.height());
            invalidate(rect);
        }
    }

    public void redo(Object action, Stroke redo) {
        if (action.equals(2)) {//cut
            undo(redo);
        }
        if (action.equals(3)) {//delete
            undo(redo);
        }
        if (action.equals(4)) {//paste
            redo(redo);
        }
    }

   /* //处理双指缩放
    private boolean handleZoom(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            isDoubleFinger = true;

            if (isFirst) { //第一次触屏
                oldLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2)
                        + Math.pow(event.getY(1) - event.getY(0), 2));//得到线段长度

                //除掉因缩放而误点屏幕上的点（双指缩放时，很大概率都是一前一后触屏，当时间差小于规定值，则视为是误点）
                mIntervalTime = event.getEventTime() - mFirstFingerDownTime;
//                if(mIntervalTime > 0 && mIntervalTime < mMaxIntervalTime){
//                    Workspace.mDrawingToolManager.OnUp(new Point((int)event.getX(0), (int)event.getY(0)),
//                            1.0f);//抬起，结束最后一条轨迹
//                    Workspace.mHistoryManager.UndoOfErrorPath();
//                }
                mOldMidPoint = getMidPoint(event);//获取缩放中心（双指中点）
                isFirst = false;
                return true;
            }

            if (mIntervalTime >= 0 && mIntervalTime < mMaxIntervalTime) { //双指缩放
                float newLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2)
                        + Math.pow(event.getY(1) - event.getY(0), 2));
                // 获取本次的缩放比例
                if (oldLineDistance != 0) {
                    rate = oldRate * newLineDistance / oldLineDistance;
                }
                if (rate < mMinRate) {
                    rate = mMinRate;
                }
                if (rate > mMaxRate) {
                    rate = mMaxRate;
                }

                //midPoint = getMidPoint(event);//获取缩放中心（双指中点）
                PointF tempPoint = getMidPoint(event);
                midPoint.x = tempPoint.x + (mOldMidPoint.x - tempPoint.x) * 2;
                midPoint.y = tempPoint.y + (mOldMidPoint.y - tempPoint.y) * 2;
                invalidate();
                if (mZoomCanvasListener != null) {
                    mZoomCanvasListener.displayZoomWindow(true, mStrokeBitmap);
                }
            }
            return true;//双指缩放时，单指失效

        } else {
            isFirst = true;
            oldRate = rate;
            return false;
        }
    }*/

    //处理双指操作
    private boolean handleTwoFingers(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            isDoubleFinger = true;

            if (isFirst) { //第一次触屏
                oldLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2)
                        + Math.pow(event.getY(1) - event.getY(0), 2));

                //除掉因缩放而误点屏幕上的点
                mIntervalTime = event.getEventTime() - mFirstFingerDownTime;
                if (mIntervalTime > 0 && mIntervalTime < mMaxIntervalTime) {
                    mStrokeCollection.endStroke(event.getDownTime());
                }

                mOldMovePoint = getMidPoint(event);
                isFirst = false;
                return true;
            }

            if (mIntervalTime >= 0 && mIntervalTime < mMaxIntervalTime) {
                float newLineDistance = (float) Math.sqrt(Math.pow(event.getX(1) - event.getX(0), 2)
                        + Math.pow(event.getY(1) - event.getY(0), 2));

                mNewMovePoint = getMidPoint(event);
                mTranslateX = mOldTranslateX + (mNewMovePoint.x - mOldMovePoint.x);
                mTranslateY = mOldTranslateY + (mNewMovePoint.y - mOldMovePoint.y);

                if (oldLineDistance != 0) {
                    rate = oldRate * newLineDistance / oldLineDistance;
                }
                if (rate < mMinRate) {
                    rate = mMinRate;
                }
                if (rate > mMaxRate) {
                    rate = mMaxRate;
                }

                limitTranslate();

                invalidate();
                if (mZoomCanvasListener != null) {
                    mZoomCanvasListener.displayZoomWindow(true, mStrokeBitmap, rate, mTranslateX, mTranslateY);
                }
            }
            return true;//双指缩放时，单指失效

        } else {
            isFirst = true;
            oldRate = rate;
            mOldTranslateX = mTranslateX;
            mOldTranslateY = mTranslateY;
            return false;
        }
    }

    //1920*0.1=192 1200*0.1=120
    private void limitTranslate() {
        if (PageEditorActivity.canvasRotation == 0 || PageEditorActivity.canvasRotation == 180) {
            if (mTranslateX < -192 * rate) {
                mTranslateX = -192 * rate;
            }
            if (mTranslateX > 192 * rate) {
                mTranslateX = 192 * rate;
            }
            if (mTranslateY < -120 * rate) {
                mTranslateY = -120 * rate;
            }
            if (mTranslateY > 120 * rate) {
                mTranslateY = 120 * rate;
            }
        } else if (PageEditorActivity.canvasRotation == 90 || PageEditorActivity.canvasRotation == 270) {
            if (mTranslateX < -120 * rate) {
                mTranslateX = -120 * rate;
            }
            if (mTranslateX > 120 * rate) {
                mTranslateX = 120 * rate;
            }
            if (mTranslateY < -192 * rate) {
                mTranslateY = -192 * rate;
            }
            if (mTranslateY > 192 * rate) {
                mTranslateY = 192 * rate;
            }
        }
    }

    //取缩放中心
    private PointF getMidPoint(MotionEvent event) {
        float x = (event.getX(1) + event.getX(0)) / 2;
        float y = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(x, y);
    }

    //取缩放后的点
    private Point getZoomPoint(Point point) {
        int x = (int) ((point.x - midPoint.x) / rate + midPoint.x);
        int y = (int) ((point.y - midPoint.y) / rate + midPoint.y);
        return new Point(x, y);
    }

    public void deleteStrokeByPoint(int x, int y) {
        Rect refreshRect = mStrokeCollection.erase(new Point(x, y));

        if (refreshRect.width() != 0 && refreshRect.height() != 0) {
            mStrokeBitmap.setPixels(mStrokeViewPixels,
                    refreshRect.top * mWidth + refreshRect.left,
                    mWidth,
                    refreshRect.left,
                    refreshRect.top,
                    refreshRect.width(),
                    refreshRect.height());
            invalidate(refreshRect);
        }
    }

    /**
     * Read strokes from file(stroke.xml).(If onLayout is not called yet,delay
     * the call of this method after it.)
     *
     * @param strokes
     */
    protected void setStrokeList(List<Stroke> strokes) {
        clearStrokeView();
        int strokeSize = strokes.size();
        for (int i = 0; i < strokeSize; i++) {
            mStrokeCollection.addStroke(strokes.get(i));
        }
        mStrokeBitmap.setPixels(mStrokeViewPixels, 0, mWidth, 0, 0, mWidth, mHeight);

        invalidate();
    }

    public LinkedList<Stroke> getStrokeList() {
        if (null == mStrokeCollection) {
            return null;
        }
        return new LinkedList<Stroke>(mStrokeCollection.getStrokes());
    }

    public void setStyleType(StrokeStyleType styleType) {
        mCurrentStyleType = styleType;
    }

    public StrokeStyleType getStyleType() {
        return mCurrentStyleType;
    }

    public void setWidthType(StrokeWidthType widthType) {
        mCurrentWidthType = widthType;
    }

    public StrokeWidthType getWidthType() {
        return mCurrentWidthType;
    }

    public void setColorType(int color) {
        mCurrentColor = color;
    }

    public int getColorType() {
        return mCurrentColor;
    }

    public void clearStrokeView() {
        if (mStrokeCollection != null) {
            mStrokeCollection.clearBackground();
        }
        Rect refreshRect = new Rect(0, 0, mWidth, mHeight);
        mStrokeBitmap.setPixels(mStrokeViewPixels, refreshRect.top * mWidth + refreshRect.left,
                mWidth, refreshRect.left, refreshRect.top, refreshRect.width(),
                refreshRect.height());
        invalidate(refreshRect);
    }

    /**
     * 释放类中资源
     */
    public void releaseStrokeViewResource() {
        mBackgroundPixels = null;
        mStrokeViewPixels = null;
        if (mStrokeCollection != null) {
            mStrokeCollection.clearBackground();
            mStrokeCollection = null;
        }
        freeBitmap();
        System.gc();
    }

    //去除缩放及平移状态
    public void clearTwoFingerState() {
        rate = 1;
        oldRate = 1;
        mTranslateX = 0;
        mTranslateY = 0;
        mOldTranslateX = 0;
        mOldTranslateY = 0;

        invalidate();

        EditorState.getInstance().setCanvasRate(rate);
        EditorState.getInstance().setCanvasTranslateX(mTranslateX);
        EditorState.getInstance().setCanvasTranslateY(mTranslateY);
    }

    private void freeBitmap() {
        if (mStrokeBitmap != null && !mStrokeBitmap.isRecycled()) {
            mStrokeBitmap.recycle();
        }
        mStrokeBitmap = null;
//        System.gc();
    }

    /**
     * Set writeMode. If value is true,it can write stroke. If value is false,it
     * does nothing.
     *
     * @param bool
     */
    protected void setWriteMode(boolean bool) {
        this.mWriteMode = bool;
    }

//    public boolean getLayoutCompleted() {
//        return mOnLayoutFlag;
//    }

    public Rect getSelectedRect() {
        if (selectStartX > selectEndX) {
            float temp = selectStartX;
            selectStartX = selectEndX;
            selectEndX = temp;
        }

        if (selectStartY > selectEndY) {
            float temp = selectStartY;
            selectStartY = selectEndY;
            selectEndY = temp;
        }

        Rect rect = new Rect();
        if (PageEditorActivity.canvasRotation == 0 || PageEditorActivity.canvasRotation == 180) {
            rect.left = Math.max((int) selectStartX - 20, 0);
            rect.top = Math.max((int) selectStartY - 20, 0);
            rect.right = Math.min((int) selectEndX + 20, PageEditorActivity.screenWidth);
            rect.bottom = Math.min((int) selectEndY + 20, PageEditorActivity.screenHeight);
        } else if (PageEditorActivity.canvasRotation == 90 || PageEditorActivity.canvasRotation == 270) {
            rect.left = Math.max((int) selectStartX - 20, 0);
            rect.top = Math.max((int) selectStartY - 20, 0);
            rect.right = Math.min((int) selectEndX + 20, PageEditorActivity.screenHeight);
            rect.bottom = Math.min((int) selectEndY + 20, PageEditorActivity.screenWidth);
        }

        if (rect.left > rect.right) {
            int temp = rect.left;
            rect.left = rect.right;
            rect.right = temp;
        }

        if (rect.top > rect.bottom) {
            int temp = rect.top;
            rect.top = rect.bottom;
            rect.bottom = temp;
        }

        return rect;
    }

    public LinkedList<Stroke> getSelectedStrokeList(Rect selectRect) {
        LinkedList<Stroke> strokeList = new LinkedList<Stroke>();

        if (selectRect != null) {
            for (int i = 0; i < getStrokeList().size(); i++) {
                Stroke stroke = getStrokeList().get(i);
                Rect strokeRect = stroke.getBounds();
                if (strokeRect.left >= selectRect.left
                        && strokeRect.top >= selectRect.top
                        && strokeRect.right <= selectRect.right
                        && strokeRect.bottom <= selectRect.bottom) {
                    strokeList.add(stroke);
                }
            }
        }

        return strokeList;
    }

    public void removeSelectedStrokeList(LinkedList<Stroke> list) {
        LinkedList<Stroke> strokeList = list;
        if (strokeList != null) {
            for (int i = 0; i < strokeList.size(); i++) {
                Rect rect = mStrokeCollection.removeStroke(strokeList.get(i));
                if (rect.width() != 0 && rect.height() != 0) {
                    mStrokeBitmap.setPixels(mStrokeViewPixels,
                            rect.top * mWidth + rect.left,
                            mWidth,
                            rect.left,
                            rect.top,
                            rect.width(),
                            rect.height());
                    invalidate(rect);
                }
            }
        }
    }

    public LinkedList<Stroke> pasteSelectedStrokeList(Rect selectedRect, LinkedList<Stroke> srcStrokeList) {
        LinkedList<Stroke> resultStrokeList = new LinkedList<>();

        if (selectedRect == null || srcStrokeList == null) {
            return resultStrokeList;
        }

        if (mPasteStartPoint.x + selectedRect.width() > this.getWidth()) {
            mPasteStartPoint.x = this.getWidth() - selectedRect.width();
        }

        if (mPasteStartPoint.y + selectedRect.height() > this.getHeight()) {
            mPasteStartPoint.y = this.getHeight() - selectedRect.height();
        }

        int differenceX = mPasteStartPoint.x - selectedRect.left;
        int differenceY = mPasteStartPoint.y - selectedRect.top;
        Log.e("wangkun", "pasteSelectedStrokeList: selectRectLeft " + selectedRect.left);
        Log.e("wangkun", "pasteSelectedStrokeList: selectRectRight" + selectedRect.right);
        Log.e("wangkun", "pasteSelectedStrokeList: differenceX " + differenceX);
        Log.e("wangkun", "pasteSelectedStrokeList: differenceY " + differenceY);

        for (int i = 0; i < srcStrokeList.size(); i++) {
            List<Point> pointList = new ArrayList<Point>();
            List<Float> pressList = new ArrayList<Float>();
            Rect rect = new Rect();

            List<Point> srcPointList = srcStrokeList.get(i).getHWPath().getPoints();
            List<Float> srcPressList = srcStrokeList.get(i).getHWPath().getPrs();
            Rect srcRect = srcStrokeList.get(i).getBounds();
            for (int j = 0; j < srcPointList.size() - 1; j++) {
                Point srcPoint = srcPointList.get(j);
                Point point = new Point(srcPoint.x + differenceX, srcPoint.y + differenceY);
                Float press = srcPressList.get(j);
                pointList.add(point);
                pressList.add(press);
            }
            pointList.add(StrokeCollection.ENDPOINT);
            pressList.add(1.0f);

            rect.left = srcRect.left + differenceX;
            rect.top = srcRect.top + differenceY;
            rect.right = srcRect.right + differenceX;
            rect.bottom = srcRect.bottom + differenceY;

            int style = srcStrokeList.get(i).getHWPen().getStyle();
            int color = srcStrokeList.get(i).getHWPen().getColor();
            int width = srcStrokeList.get(i).getHWPen().getWidth();
            int[] mColors = srcStrokeList.get(i).getHWPen().getColors();

            Stroke newStroke = new Stroke(style, color, width, true, pointList, pressList);
            newStroke.getHWPen().setColors(mColors);
            newStroke.setBounds(rect);

            Rect refreshRect = mStrokeCollection.addStroke(newStroke);

            if (refreshRect.width() != 0 && refreshRect.height() != 0) {
                mStrokeBitmap.setPixels(mStrokeViewPixels,
                        refreshRect.top * mWidth + refreshRect.left,
                        mWidth,
                        refreshRect.left,
                        refreshRect.top,
                        refreshRect.width(),
                        refreshRect.height());
                invalidate(refreshRect);
            }

            resultStrokeList.add(newStroke);
        }

        return resultStrokeList;
    }
}
