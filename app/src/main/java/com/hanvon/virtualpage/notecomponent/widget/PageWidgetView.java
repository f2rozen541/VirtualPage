package com.hanvon.virtualpage.notecomponent.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.notecomponent.adapter.PageWidgetAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 翻页仿生控件
 * @Author: TaoZhi
 * @Date: 2016/3/31
 * @E_mail: taozhi@hanwang.com.cn
 */
public class PageWidgetView extends FrameLayout {

    private static int ANIMATE_DURATION = 500;
    private int fractionTime = 500;

    private int mWidth = 0;
    private int mHeight = 0;
    private int mCornerX = 0; // 拖拽点对应的页脚
    private int mCornerY = 0;
    private Path mPath0;
    private Path mPath1;


    PointF mTouch = new PointF(); // 拖拽点
    PointF mBezierStart1 = new PointF(); // 贝塞尔曲线起始点
    PointF mBezierControl1 = new PointF(); // 贝塞尔曲线控制点
    PointF mBezierVertex1 = new PointF(); // 贝塞尔曲线顶点
    PointF mBezierEnd1 = new PointF(); // 贝塞尔曲线结束点

    PointF mBezierStart2 = new PointF(); // 另一条贝塞尔曲线
    PointF mBezierControl2 = new PointF();
    PointF mBezierVertex2 = new PointF();
    PointF mBezierEnd2 = new PointF();

    PointF mLT = new PointF();
    PointF mRT = new PointF();
    PointF mLB = new PointF();
    PointF mRB = new PointF();
    PointF mBzTemp;
    PointF mBzTempStart = new PointF();

    float mMiddleX;
    float mMiddleY;
    float mDegrees;
    float mTouchToCornerDis;
    ColorMatrixColorFilter mColorMatrixFilter;
    Matrix mMatrix;
    float[] mMatrixArray = {0, 0, 0, 0, 0, 0, 0, 0, 1.0f};

    boolean mIsRT_And_LB; // 是否属于右上或者左下
    float mMaxLength;
    int[] mBackShadowColors;
    int[] mFrontShadowColors;
    GradientDrawable mBackShadowDrawableLR;
    GradientDrawable mBackShadowDrawableRL;
    GradientDrawable mFolderShadowDrawableLR;
    GradientDrawable mFolderShadowDrawableRL;

    GradientDrawable mFrontShadowDrawableHBT;
    GradientDrawable mFrontShadowDrawableHTB;
    GradientDrawable mFrontShadowDrawableVLR;
    GradientDrawable mFrontShadowDrawableVRL;

    Paint mPaint;

    Paint mPointPaint;

    Scroller mScroller;
    private boolean isAnimated = false;
    private View currentView = null; // 当前显示视图
    private View nextView = null; // 翻页后显示视图
    private View nextViewTranscript = null; // 翻页后显示视图副本，用于翻页过程中当前页背面的显示
    private Context mContext;

    private PageWidgetAdapter mAdapter = null;
    private int currentPosition = 0;
    private int itemCount = 0;
    private OnPageItemListener turnListener;
    private int mLeftOffsetWidth = -1;
    private boolean isAutoAnimate = false;

    private boolean isActionClick = true;
    private static final float TOLERATE_DIST = 20;
    private float moveX = -1;
    private float moveY = -1;
    private int mDestPosition = 0;

    public PageWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mContext = context;
        viewInit();
    }


    public PageWidgetView(Context context) {
        this(context, null);
    }

    private void viewInit() {
        mPath0 = new Path();
        mPath1 = new Path();
        createDrawable();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.RED);
        mPointPaint.setStyle(Paint.Style.FILL);

        // 构建一个转置矩阵过滤器
        ColorMatrix cm = new ColorMatrix();
        float array[] = {
                0.55f, 0, 0, 0,
                80.0f, 0, 0.55f, 0,
                0, 80.0f, 0, 0,
                0.55f, 0, 80.0f, 0,
                0, 0, 0.2f, 0
        };
        cm.set(array);
        mColorMatrixFilter = new ColorMatrixColorFilter(cm);

        mMatrix = new Matrix();
        mScroller = new Scroller(mContext);

        mAdapter = new PageWidgetAdapter(mContext);

        setOnTouchListener(new FingerTouchListener());

    }


    private class FingerTouchListener implements OnTouchListener {
        private float startX, startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (v == PageWidgetView.this && mAdapter != null) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (itemCount == 0) {
                            return false;
                        }
                        startX = event.getX();
                        startY = event.getY();
                        moveX = 0;
                        moveY = 0;
                        calcCornerXY(startX, startY);
                        Log.d("tz", "ACTION_DOWN() called with: (mTouch.x,mTouch.y)---->(" + mTouch.x + ", " + mTouch.y + ")");
                        if (isAnimated) {
                            abortAnimation();
                        }

                        if (currentPosition == 1 && DragToRight()) { // 如果是在cover页向右翻页，就执行关闭操作 2016年5月26日 15:22:44
                            if (turnListener != null) {
                                turnListener.onClickOnNoteCover();
                                return false;
                            }
                        }
//                        if (isOutOfBoundary()) { // 如果是第一页往右翻页或者最后一页往左拖拽，就直接返回了
//                            return false;
//                        }
                        isActionClick = true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // 获取触摸点的坐标值， 并且刷新显示
                        float x = event.getX();
                        float y = event.getY();
                        moveX += (x - startX);
                        moveY += (y - startY);
                        startX = x;
                        startY = y;
                        if (Math.abs(moveX) > TOLERATE_DIST || Math.abs(moveY) > TOLERATE_DIST) { // 认为是在滑动了
                            isActionClick = false;
                        }
                        if (isOutOfBoundary()) { // 如果是第一页往右翻页或者最后一页往左拖拽，就直接返回了
                            return false;
                        }
                        if (!isActionClick) { // 如果不是点击事件，那就一定是拖拽事件
                            if (x > mWidth) { // 防止越界
                                mTouch.x = mWidth - 0.01f;
                            } else if (x < 0) {
                                mTouch.x = 0.01f;
                            } else {
                                mTouch.x = x;
                            }
                            if (y > mHeight) { // 防止越界
                                mTouch.y = mHeight - 0.01f;
                            } else if (y < 0) {
                                mTouch.y = 0.01f;
                            } else {
                                mTouch.y = y;
                            }
//                            Log.d("tz", "onTouch() returned: mTouch.x---mTouch.y---mCornerX---mCornerY");
//                            Log.d("tz", "onTouch() returned:" + mTouch.x + "---" + mTouch.y + "---" + mCornerX + "---" + mCornerY);
                            calcPoints();
                            if (DragToRight()) { // 向右翻页
                                if (currentPosition == 0) { // 第一页
                                    mCornerX = 0;
                                    mCornerY = 0;
                                    return false;
                                }
                                // 将需要用到的View加载进来
                                nextView = mAdapter.getView(currentPosition - 1, nextView, null);
                                nextViewTranscript = mAdapter.getView(currentPosition - 1, nextViewTranscript, null);
                            } else { // 向左翻页
                                if (currentPosition == itemCount - 1) { // 最后一页
//                                    mCornerX = 0;
//                                    mCornerY = 0;
                                    return false;
                                }
                                // 将需要用到的View加载进来
                                nextView = mAdapter.getView(currentPosition + 1, nextView, null);
                                nextViewTranscript = mAdapter.getView(currentPosition + 1, nextViewTranscript, null);
                            }
                            nextView.setVisibility(View.VISIBLE);
                            nextViewTranscript.setVisibility(View.VISIBLE);
                            PageWidgetView.this.postInvalidate();
                        }
//                        Log.i("tz", "ACTION_MOVE() called with: (mTouch.x,mTouch.y)---->(" + mTouch.x + ", " + mTouch.y + ")");
//                        PageWidget.this.postInvalidate();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (isActionClick) {
                            resetTouchAndCorner();
                            if (turnListener != null && currentPosition > 0) {
                                turnListener.onPageClick(currentPosition);
                            }
                        } else {
                            if ((DragToRight() && currentPosition == 0) || (!DragToRight() && currentPosition == itemCount - 1)) {
                                return false;
                            }
                            if (canDragOver()) {
                                if (DragToRight()) {
                                    mDestPosition = currentPosition - 1;
                                } else {
                                    mDestPosition = currentPosition + 1;
                                }
                                isAnimated = true;
                                startAnimation(ANIMATE_DURATION);
                            } else {
                                resetTouchAndCorner();
                                nextView.setVisibility(View.INVISIBLE);
                                nextViewTranscript.setVisibility(View.INVISIBLE);
                            }
                        }
                        PageWidgetView.this.postInvalidate();
                        break;
                }
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mWidth == 0) {
            mWidth = getWidth();
            mHeight = getHeight();

            mTouch.x = 0.01f; // 不让x,y为0,否则在点计算时会有问题
            mTouch.y = 0.01f;
            mLT.x = 0;
            mLT.y = 0;
            mLB.x = 0;
            mLB.y = mHeight;
            mRT.x = mWidth;
            mRT.y = 0;
            mRB.x = mWidth;
            mRB.y = mHeight;
            mMaxLength = (float) Math.hypot(mWidth, mHeight);
        }
    }

    /**
     * 计算拖拽点对应的拖拽脚
     */
    public void calcCornerXY(float x, float y) {
        mCornerX = x <= mWidth / 2 ? 0 : mWidth;
        mCornerY = y <= mHeight / 2 ? 0 : mHeight;

        if ((mCornerX == 0 && mCornerY == mHeight) || (mCornerX == mWidth && mCornerY == 0)) {
            mIsRT_And_LB = true;
        } else {
            mIsRT_And_LB = false;
        }
    }

    /**
     * 计算点的值
     */
    private void calcPoints() {
        mMiddleX = (mTouch.x + mCornerX) / 2;
        mMiddleY = (mTouch.y + mCornerY) / 2;

        mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
        mBezierControl1.y = mCornerY;

        if (isAnimated) { // 如果开始动画了，就需要修正水平控制点的位置
            adjustVerticalBezierControlPoint();
        }

        mBezierControl2.x = mCornerX;
        mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

        mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 3;
        mBezierStart1.y = mCornerY;

        // 当mBezierStart1.x < 0或者mBezierStart1.x > mWidth时
        // 如果继续翻页，会出现BUG故在此限制
        if (!isAnimated) {
            if (mCornerX == 0 && mBezierStart1.x > mWidth / 2) {
                float f1 = Math.abs(mCornerX - mTouch.x);

                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);

                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);

                mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 3;
            }
            if (mCornerX == mWidth && mBezierStart1.x < mWidth / 2) {
                mBezierStart1.x = mWidth - mBezierStart1.x;
                float f1 = Math.abs(mCornerX - mTouch.x);

                float f2 = mWidth / 2 * f1 / mBezierStart1.x;
                mTouch.x = Math.abs(mCornerX - f2);

                float f3 = Math.abs(mCornerX - mTouch.x) * Math.abs(mCornerY - mTouch.y) / f1;
                mTouch.y = Math.abs(mCornerY - f3);

                mMiddleX = (mTouch.x + mCornerX) / 2;
                mMiddleY = (mTouch.y + mCornerY) / 2;

                mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY) * (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
                mBezierControl1.y = mCornerY;

                mBezierControl2.x = mCornerX;
                mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX) * (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
                mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x) / 3;
            }
        }

        mBezierStart2.x = mCornerX;
        mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y) / 3;

        mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX), (mTouch.y - mCornerY));

        mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1, mBezierStart2);
        mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1, mBezierStart2);

		/*
         * mBezierVertex1.x 推导
		 * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2 化简等价于
		 * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
		 */
        mBezierVertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;
        mBezierVertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;
        mBezierVertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;
        mBezierVertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;
    }

    /**
     * 调整水平方向上贝塞尔曲线控制点的坐标值
     * 根据公式推导出，当控制点控制贝塞尔曲线起始点为mWidth/2的时候，limitBC1=1/8mWidth，
     * 为了让缝隙与中线重合，让起始点坐标继续再平移1/2*limitBC1根据翻页方向计算
     */
    private void adjustVerticalBezierControlPoint() {
        int limitBC1;// X方向上的控制点的坐标值的标线值
        if (mCornerX > 0) { // 如果是向左翻页
            limitBC1 = mWidth * 9 / 16;
            if (limitBC1 > mBezierControl1.x) { // 如果低于标线值，就等于标线值
                mBezierControl1.x = limitBC1;
            }
        } else { // 如果是向右翻页
            limitBC1 = mWidth * 7 / 16;
            if (limitBC1 < mBezierControl1.x) { // 如果超过标线值，就等于标线值
                mBezierControl1.x = limitBC1;
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        calcPoints();
        super.dispatchDraw(canvas);
        if (itemCount > 1) {
            drawCurrentPageShadow(canvas);
            drawCurrentBackArea(canvas, nextViewTranscript);
//            drawControlPoints(canvas);
        }

    }

    private void drawControlPoints(Canvas canvas) {
        mPointPaint.setColor(Color.RED);
        canvas.drawCircle(mBezierControl1.x, mBezierControl1.y, 10, mPointPaint);
        canvas.drawCircle(mBezierControl2.x, mBezierControl2.y, 10, mPointPaint);
        mPointPaint.setColor(Color.BLUE);
        canvas.drawCircle(mBezierStart1.x, mBezierStart1.y, 10, mPointPaint);
        canvas.drawCircle(mBezierEnd1.x, mBezierEnd1.y, 10, mPointPaint);
        canvas.drawCircle(mBezierStart2.x, mBezierStart2.y, 10, mPointPaint);
        canvas.drawCircle(mBezierEnd2.x, mBezierEnd2.y, 10, mPointPaint);
        mPointPaint.setColor(Color.YELLOW);
        canvas.drawCircle(mBezierVertex1.x, mBezierVertex1.y, 10, mPointPaint);
        canvas.drawCircle(mBezierVertex2.x, mBezierVertex2.y, 10, mPointPaint);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child.equals(currentView)) {
            drawCurrentPageArea(canvas, child, mPath0); // 画当前Page
        } else {
            drawNextPageAreaAndShadow(canvas, child); // 画背面Page
        }
        return true;
    }

    /**
     * 主要就是计算偏移值，并且利用mScroller进行数值计算
     *
     * @param delayMillis 动画持续时间
     */
    private void startAnimation(int delayMillis) {

        int dx, dy;// dx 水平方向滑动的距离，负值会使滚动向左滚动，dy 垂直方向滑动的距离，负值会使滚动向上滚动
        if (mCornerX > 0) {
            dx = (int) (-mTouch.x + 1);
        } else {
            dx = (int) (mWidth - mTouch.x - 1);
        }
        if (mCornerY > 0) {
            dy = (int) (mHeight - mTouch.y - 1);
        } else {
            dy = (int) (1 - mTouch.y); // 防止mTouch.y最终变为0
        }
        mScroller.startScroll((int) mTouch.x, (int) mTouch.y, dx, dy, delayMillis);

    }

    /**
     * 计算最新的Touch坐标值，如果计算停止，就将部分控件隐藏，并回调接口中的方法
     */
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) { // 不断模拟触摸点的坐标值，然后刷新页面
            float x = mScroller.getCurrX();
            float y = mScroller.getCurrY();
            mTouch.x = x;
            mTouch.y = y;
            postInvalidate();
        }
        if (mScroller.isFinished() && isAnimated) { // 如果计算模拟值已经停止abortAnimation
            isAnimated = false;
            calculateNewPosition();
            currentView = mAdapter.getView(currentPosition, currentView, null); // 获取指定位置的itemView
            resetTouchAndCorner();
            nextView.setVisibility(View.INVISIBLE);
            nextViewTranscript.setVisibility(View.INVISIBLE);
            postInvalidate();
            if (currentPosition < mDestPosition) { // 如果还没有到指定页，继续执行刷新
                movePages(1, fractionTime);
            } else if (currentPosition > mDestPosition) {
                movePages(-1, fractionTime);
            } else {
                if (turnListener != null && currentPosition > 0) { // 只有翻到了目标页的时候才表示翻页完毕
                    turnListener.onTurn(itemCount, currentPosition);
                }
            }
        }
    }

    public void jumpToSpecificPosition(int destPosition) {
        if (destPosition < 0 || destPosition >= mAdapter.getCount()) { // 不在数据范围内， 直接返回
            return;
        }
        mDestPosition = destPosition;
        int gapIndex = destPosition - currentPosition;
        if (gapIndex == 0) { // 页没有变化，直接返回
            return;
        } else if (gapIndex > 0) { // 如果是右翻到左
            if(gapIndex > 3) { // 限定翻页次数
                gapIndex = 3;
                currentPosition = mDestPosition - 3;
            }
            fractionTime = ANIMATE_DURATION / gapIndex;
            movePages(1, fractionTime);
        } else { // 如果是左翻到右
            if (gapIndex < -3) {
                gapIndex = -3;
                currentPosition = mDestPosition + 3;
            }
            fractionTime = ANIMATE_DURATION / -gapIndex;
            movePages(-1, fractionTime);
        }
    }

    public void movePages(int gapIndex){
        if (gapIndex > 0) {

        }
        movePages(gapIndex, ANIMATE_DURATION);
    }

    public void updateCurrentView() {
        currentView = mAdapter.getView(currentPosition, currentView, null);
        postInvalidate();
    }

    private void movePages(int gapIndex, int duration) {
        int newPosition = currentPosition + gapIndex;
        if (newPosition < 0) {
            newPosition = 0;
        }
        if (newPosition >= mAdapter.getCount()) {
            newPosition = mAdapter.getCount() - 1;
        }
        // 获取下一页的内容
        nextView = mAdapter.getView(newPosition, nextView, null);
        nextViewTranscript = mAdapter.getView(newPosition, nextViewTranscript, null);
        nextView.setVisibility(View.VISIBLE);
        nextViewTranscript.setVisibility(View.VISIBLE);

        isAnimated = true;
        if (gapIndex < 0) {
            mCornerX = 0;
            mCornerY = mHeight;
            mTouch.x = 0;
            mTouch.y = mHeight - 150.01f;
        } else {
            mCornerX = mWidth;
            mCornerY = mHeight;
            mTouch.x = mWidth - 150.01f;
            mTouch.y = mHeight - 150.01f;
        }
        startAnimation(duration); // 最后一次动画时间延长
        postInvalidate();
    }


    /**
     * 让mScroller停止计算
     */
    public void abortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            isAnimated = false;
            currentPosition = mDestPosition; // 如果停止动画，就让当前位置直接变为目标位置
            currentView = mAdapter.getView(currentPosition, currentView, null); // 获取指定位置的itemView
            nextView.setVisibility(View.INVISIBLE);
            nextViewTranscript.setVisibility(View.INVISIBLE);
            postInvalidate();
        }
    }

    private void calculateNewPosition() {
        if (DragToRight()) {
            currentPosition--;
            if (currentPosition < 0) {
                currentPosition = 0;
            }
        } else {
            currentPosition++;
            if (currentPosition > itemCount - 1) {
                currentPosition = itemCount - 1;
            }
        }
    }

    public void setStartWithCover(Page cover, List<Page> pages) {
        List<Page> demoList = new ArrayList<>();
        if (cover != null) demoList.add(cover);
        if (pages != null) demoList.addAll(pages);
        mAdapter.setCoverPage(cover);
        setData(demoList);
        jumpToSpecificPosition(mAdapter.getCount() - 1);
    }

    public void setStartWithCoverToPosition(Page cover, List<Page> pages, int position) {
        List<Page> demoList = new ArrayList<>();
        if (cover != null) demoList.add(cover);
        if (pages != null) demoList.addAll(pages);
        mAdapter.setCoverPage(cover);
        setData(demoList);
        currentPosition = 0;
//        mDestPosition = position;
//        jumpToSpecificPosition(position);
    }

    public void updateDataWithCover(List<Page> pages) {
        List<Page> demoList = new ArrayList<>();
        Page cover = mAdapter.getCoverPage();
        if (cover != null) demoList.add(cover);
        if (pages != null) demoList.addAll(pages);
        mAdapter.setData(demoList);
        updateCurrentView();
    }

    private void setData(List<Page> pageList) {
        mAdapter.setData(pageList);
        initData();
    }

    public void setAdapter(PageWidgetAdapter adapter) {
        mAdapter = adapter;
        initData();

    }

    private void initData() {
        itemCount = mAdapter.getCount();
        currentView = null;
        nextView = null;
        nextViewTranscript = null;
        removeAllViews();
        if (itemCount != 0) {
            currentPosition = 0;
            currentView = mAdapter.getView(currentPosition, null, null); // 取得实例
            addView(currentView); // 添加到父视图里
            if (itemCount > 1) {
                nextView = mAdapter.getView(currentPosition, null, null); // 取得实例，添加
                nextViewTranscript = mAdapter.getView(currentPosition, null, null);
                nextView.setVisibility(View.INVISIBLE);
                nextViewTranscript.setVisibility(View.INVISIBLE);
                addView(nextView);
                addView(nextViewTranscript);
            }

        } else {
            currentPosition = -1;
        }
        resetTouchAndCorner();
        postInvalidate();
    }


    private void resetTouchAndCorner() {
        mTouch.x = 0.01f;
        mTouch.y = 0.01f;
        mCornerX = 0;
        mCornerY = 0;
    }

    /**
     * 用于翻页结束后的页码通知
     */
    public interface OnPageItemListener {
        void onTurn(int count, int currentPosition);

        void onPageClick(int currentPosition);

        void onClickOnNoteCover();
    }

    public void setOnPageTurnListener(OnPageItemListener listener) {
        turnListener = listener;
    }

    /**
     * 创建阴影的GradientDrawable
     */
    private void createDrawable() {
        int[] color = {0x333333, 0xb0333333};
        mFolderShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, color);
        mFolderShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFolderShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, color);
        mFolderShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowColors = new int[]{0xff111111, 0x111111};
        mBackShadowDrawableRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
        mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mBackShadowDrawableLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
        mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowColors = new int[]{0x80111111, 0x111111};
        mFrontShadowDrawableVLR = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
        mFrontShadowDrawableVLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableVRL = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
        mFrontShadowDrawableVRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHTB = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
        mFrontShadowDrawableHTB.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        mFrontShadowDrawableHBT = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
        mFrontShadowDrawableHBT.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    }

    private void drawCurrentPageArea(Canvas canvas, View child, Path path) {
        mPath0.reset();
        mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x, mBezierEnd1.y);
        mPath0.lineTo(mTouch.x, mTouch.y);
        mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x, mBezierStart2.y);
        mPath0.lineTo(mCornerX, mCornerY);
        mPath0.close();

        canvas.save();
        canvas.clipPath(path, Region.Op.XOR);// 这里即裁剪出了当前页应该绘制的区域
        child.draw(canvas); //这里再将canvas交给子视图绘制
        canvas.restore();
    }

    /**
     * 画当前页的背面的内容
     * @param canvas 当前画布
     * @param child 背面的视图View
     */
    private void drawNextPageAreaAndShadow(Canvas canvas, View child) {
        mPath1.reset();
        mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.lineTo(mBezierVertex1.x, mBezierVertex1.y);
        mPath1.lineTo(mBezierVertex2.x, mBezierVertex2.y);
        mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
        mPath1.lineTo(mCornerX, mCornerY);
        mPath1.close();

        mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x - mCornerX, mBezierControl2.y - mCornerY));
        int leftX;
        int rightX;
        GradientDrawable mBackShadowDrawable;
        if (mIsRT_And_LB) {
            leftX = (int) (mBezierStart1.x);
            rightX = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
            mBackShadowDrawable = mBackShadowDrawableLR;
        } else {
            leftX = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
            rightX = (int) mBezierStart1.x;
            mBackShadowDrawable = mBackShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT); // 这里裁剪出下一页应该绘制的区域
        child.draw(canvas); // 这里子视图开始绘制
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y); //这里旋转是用来画阴影的
        mBackShadowDrawable.setBounds(
                leftX, (int) mBezierStart1.y,
                rightX, (int) (mMaxLength + mBezierStart1.y));
        mBackShadowDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制翻起页的阴影
     */
    private void drawCurrentPageShadow(Canvas canvas) {
        double degree;
        if (mIsRT_And_LB) {
            degree = Math.PI / 4 - Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x - mBezierControl1.x);
        } else {
            degree = Math.PI / 4 - Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x - mBezierControl1.x);
        }

        if (Math.toDegrees(degree) > 220) {
            return;
        }

        // 翻起页阴影顶点与touch点的距离
        double d1 = (float) 25 * 1.414 * Math.cos(degree);
        double d2 = (float) 25 * 1.414 * Math.sin(degree);
        float x = (float) (mTouch.x + d1);
        float y;
        if (mIsRT_And_LB) {
            y = (float) (mTouch.y + d2);
        } else {
            y = (float) (mTouch.y - d2);
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
        mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
        mPath1.close();
        float rotateDegrees;
        canvas.save();

        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        int leftX;
        int rightX;
        GradientDrawable mCurrentPageShadow;
        if (mIsRT_And_LB) {
            leftX = (int) (mBezierControl1.x);
            rightX = (int) mBezierControl1.x + 25;
            mCurrentPageShadow = mFrontShadowDrawableVLR;
        } else {
            leftX = (int) (mBezierControl1.x - 25);
            rightX = (int) mBezierControl1.x + 1;
            mCurrentPageShadow = mFrontShadowDrawableVRL;
        }

        rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x - mBezierControl1.x, mBezierControl1.y - mTouch.y));
        canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
        mCurrentPageShadow.setBounds(
                leftX, (int) (mBezierControl1.y - mMaxLength),
                rightX, (int) (mBezierControl1.y));
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

        int offset = mCornerX > 0 ? 30 : -30;

        if (mBezierControl2.y < 0) {
            mBzTemp = getCross(mLT, mRT, mTouch, mBezierControl2);
            mBzTempStart.x = mBzTemp.x - offset;
            mBzTempStart.y = mBzTemp.y;
        } else if (mBezierControl2.y > mHeight) {
            mBzTemp = getCross(mLB, mRB, mTouch, mBezierControl2);
            mBzTempStart.x = mBzTemp.x - offset;
            mBzTempStart.y = mBzTemp.y;
        } else {
            mBzTemp = mBezierControl2;
            mBzTempStart = mBezierStart2;
        }
        mPath1.reset();
        mPath1.moveTo(x, y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBzTemp.x, mBzTemp.y);
        mPath1.lineTo(mBzTempStart.x, mBzTempStart.y);
        mPath1.close();
        canvas.save();
        canvas.clipPath(mPath0, Region.Op.XOR);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);
        if (mIsRT_And_LB) {
            leftX = (int) (mBzTemp.y);
            rightX = (int) (mBzTemp.y + 25);
            mCurrentPageShadow = mFrontShadowDrawableHTB;
        } else {
            leftX = (int) (mBzTemp.y - 25);
            rightX = (int) (mBzTemp.y);
            mCurrentPageShadow = mFrontShadowDrawableHBT;
        }
        rotateDegrees = (float) Math.toDegrees(Math.atan2(mBzTemp.y
                - mTouch.y, mBzTemp.x - mTouch.x));
        canvas.rotate(rotateDegrees, mBzTemp.x, mBzTemp.y);
        mCurrentPageShadow.setBounds(
                (int) (mBzTemp.x - mMaxLength), leftX,
                (int) (mBzTemp.x), rightX);
        mCurrentPageShadow.draw(canvas);
        canvas.restore();

    }

    /**
     * 绘制翻起页背面
     */
    private void drawCurrentBackArea(Canvas canvas, View view) {
        int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
        float f1 = Math.abs(i - mBezierControl1.x);
        /*int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);*/
        float f3 = f1;
        mPath1.reset();
        mPath1.moveTo(mBezierVertex2.x, mBezierVertex2.y);
        mPath1.lineTo(mBezierVertex1.x, mBezierVertex1.y);
        mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
        mPath1.lineTo(mTouch.x, mTouch.y);
        mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
        mPath1.close();
        GradientDrawable mFolderShadowDrawable;
        int left;
        int right;
        if (mIsRT_And_LB) {
            left = (int) (mBezierStart1.x - 1);
            right = (int) (mBezierStart1.x + f3 + 1);
            mFolderShadowDrawable = mFolderShadowDrawableLR;
        } else {
            left = (int) (mBezierStart1.x - f3 - 1);
            right = (int) (mBezierStart1.x + 1);
            mFolderShadowDrawable = mFolderShadowDrawableRL;
        }
        canvas.save();
        canvas.clipPath(mPath0);
        canvas.clipPath(mPath1, Region.Op.INTERSECT);

        //mPaint.setColorFilter(mColorMatrixFilter);

        float rotateDegrees = (float) Math.toDegrees(Math.PI / 2 + Math.atan2(mBezierControl2.y - mTouch.y, mBezierControl2.x - mTouch.x));

        if (mCornerY == 0) {
            rotateDegrees -= 180;
        }
        mMatrix.reset();
        mMatrix.setPolyToPoly(new float[]{Math.abs(mWidth - mCornerX), mCornerY}, 0, new float[]{mTouch.x, mTouch.y}, 0, 1);
        mMatrix.postRotate(rotateDegrees, mTouch.x, mTouch.y);
        canvas.save();
        canvas.concat(mMatrix);
        view.draw(canvas);
        canvas.restore();
        canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
        mFolderShadowDrawable.setBounds(
                left, (int) mBezierStart1.y,
                right + 2, (int) (mBezierStart1.y + mMaxLength));
        mFolderShadowDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 求解直线P1P2和直线P3P4的交点坐标
     */
    public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
        PointF CrossP = new PointF();
        // 二元函数通式： y=ax+b
        float a1 = (P2.y - P1.y) / (P2.x - P1.x);
        float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

        float a2 = (P4.y - P3.y) / (P4.x - P3.x);
        float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
        CrossP.x = (b2 - b1) / (a1 - a2);
        CrossP.y = a1 * CrossP.x + b1;
        return CrossP;
    }

    /**
     * 是否可以翻页
     */
    public boolean canDragOver() {
        return mTouchToCornerDis > mWidth / 5;
    }

    /**
     * 是否从左边翻向右边，如果X角标为0.则返回true
     */
    public boolean DragToRight() {
        return mCornerX == 0;
    }

    public boolean isOutOfBoundary() {
        return (currentPosition == 1 && DragToRight()) || (currentPosition == itemCount - 1 && !DragToRight());
    }

}
