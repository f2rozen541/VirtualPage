package com.hanvon.virtualpage.pageeditor.tools.picture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.common.EditorState;
import com.hanvon.virtualpage.common.ElementLayout;
import com.hanvon.virtualpage.pageeditor.activity.PageEditorActivity;
import com.hanvon.virtualpage.utils.BitmapUtil;

;

/**
 * -------------------------------
 * Description:
 * 图片对象，继承ElementLayout。
 * 提供方法：构建NotePicture，添加边框，touch时间处理（图片的缩放，移动）
 * -------------------------------
 */
public class NotePicture extends ElementLayout {
    private int mFinalWidth = 0;
    private int mFinalHeight = 0;
    private int mMinWidth = 200;
    private int mMinHeight = 200;
    private int mMaxWidth = 1000;
    private int mMaxHeight = 1000;

    private NotePictureContent mContentView;
    private Bitmap mResizedBmp;

    private int mTmpX, mTmpY;

    private Path mPath = null;
    private Paint mLinePaint = null;
    private Paint mDefaultPaint = null;

    public NotePicture(Context context, int width, int height, int boundSize) {
        super(context, boundSize);
        mFinalWidth = width;
        mFinalHeight = height;
    }

    @Override
    public void addBounds() {
        super.addBounds();
        for (int i = 0; i < mBorderList.length; i++) {
            mBorderList[i] = new NotePictureBorder_Default(getContext(), i);
            this.addView(mBorderList[i]);
        }
        for (int i = 0; i < mCornerList.length; i++) {
            mCornerList[i] = new NotePictureCorner_Default(getContext(), i);
            this.addView(mCornerList[i]);
        }
    }

    public Path getPath() {
        if(mPath == null)
            mPath = new Path();
        else
            mPath.reset();

        return mPath;
    }

    public Paint getLinePaint() {
        if(mLinePaint == null)
        {
            mLinePaint = new Paint();
            mLinePaint.setStyle(Paint.Style.STROKE);
            PathEffect effects = new DashPathEffect(new float[] { 5, 3 }, 1);
            mLinePaint.setPathEffect(effects);
        }

        return mLinePaint;
    }

    public Paint getDefaultPaint()
    {
        if(mDefaultPaint == null)
        {
            mDefaultPaint = new Paint();
            mDefaultPaint.setStyle(Paint.Style.STROKE);
        }

        return mDefaultPaint;
    }

    @Override
    public void addContentView() {
        mContentView = new NotePictureContent(getContext());
        RelativeLayout.LayoutParams rlLPcontent = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        rlLPcontent.setMargins(mBoundSize, mBoundSize, mBoundSize, mBoundSize);
        mContentView.setLayoutParams(rlLPcontent);
        mContentView.setBackgroundColor(Color.TRANSPARENT);
        mContentView.setFocusableInTouchMode(true);
        mContentView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeBounds();
                } else {
                    setBoundType(0);
                    addBounds();
                }
            }
        });
        addView(mContentView);

        if(getTag() == null){
            return;
        }

        final String path = getTag().toString();
        if (path != null && path instanceof String) {
            Bitmap bitmap = BitmapUtil.decodeFileBySize(path, mFinalWidth, mFinalHeight);
            if(bitmap != null){
                ImageView imageView = this.mContentView;
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                mResizedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), null, true);
                imageView.setImageBitmap(mResizedBmp);
            } else {
                mResizedBmp = null;
            }
        }
    }

    @Override
    public void onRemovedFromParent() {
        /*if (mCurrentElementType == ElementType.ERASER) {
            final String path = getTag().toString();
            FileManager.deleteFile(path);
        }*/
    }

    public class NotePictureContent extends ImageView {

        public NotePictureContent(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER){
                return false;
            }
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) NotePicture.this
                    .getLayoutParams();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTmpX = x;
                    mTmpY = y;
                    mContentView.requestFocus();
                   // Log.i("***", "ImageView ACTION_DOWN");
                   // Log.i("***", "param:x=" + param.x + ";y=" + param.y + ";w=" + param.width + ";h=" + param.height);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(PageEditorActivity.canvasRotation == 0){
                        param.x += x - mTmpX;
                        param.y += y - mTmpY;
                    } else if(PageEditorActivity.canvasRotation == 90){
                        param.x += y - mTmpY;
                        param.y -= x - mTmpX;
                    } else if(PageEditorActivity.canvasRotation == 180){
                        param.x -= x - mTmpX;
                        param.y -= y - mTmpY;
                    } else if(PageEditorActivity.canvasRotation == 270){
                        param.x -= y - mTmpY;
                        param.y += x - mTmpX;
                    }

                    if((PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 0)
                            || (PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 180)
                            || (PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 0)
                            || (PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 180)){
                        if(param.x < 0){
                            param.x = 0;
                        }
                        if(param.x > PageEditorActivity.screenWidth - param.width){
                            param.x = PageEditorActivity.screenWidth - param.width;
                        }
                        if(param.y < 0){
                            param.y = 0;
                        }
                        if(param.y > PageEditorActivity.screenHeight - param.height){
                            param.y = PageEditorActivity.screenHeight - param.height;
                        }
                    }

                    if((PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 90)
                            || (PageEditorActivity.rotationValueByHand == 0 && PageEditorActivity.canvasRotation == 270)
                            || (PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 90)
                            || (PageEditorActivity.rotationValueByHand == 180 && PageEditorActivity.canvasRotation == 270)){
                        if(param.y < 0){
                            param.y = 0;
                        }
                        if(param.y > PageEditorActivity.screenHeight - param.height){
                            param.y = PageEditorActivity.screenHeight - param.height;
                        }
                    }

                    if((PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 90)
                            || (PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 270)
                            || (PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 90)
                            || (PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 270)){
                        if(param.x < 0){
                            param.x = 0;
                        }
                        if(param.x > PageEditorActivity.screenHeight - param.width){
                            param.x = PageEditorActivity.screenHeight - param.width;
                        }
                        if(param.y < 0){
                            param.y = 0;
                        }
                        if(param.y > PageEditorActivity.screenWidth - param.height){
                            param.y = PageEditorActivity.screenWidth - param.height;
                        }
                    }

                    if((PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 0)
                            || (PageEditorActivity.rotationValueByHand == 90 && PageEditorActivity.canvasRotation == 180)
                            || (PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 0)
                            || (PageEditorActivity.rotationValueByHand == 270 && PageEditorActivity.canvasRotation == 180)){
                        if(param.y < 0){
                            param.y = 0;
                        }
                        if(param.y > PageEditorActivity.screenWidth - param.height){
                            param.y = PageEditorActivity.screenWidth - param.height;
                        }
                    }

                    mTmpX = x;
                    mTmpY = y;

                    NotePicture.this.setLayoutParams(param);
                    invalidate();

                   // Log.i("***", "ImageView ACTION_MOVE");
                   // Log.i("***", "param:x=" + param.x + ";y=" + param.y + ";w=" + param.width + ";h=" + param.height);
                    break;
                case MotionEvent.ACTION_UP:
                    Rect rect = new Rect();
                    rect.left = param.x;
                    rect.top = param.y;
                    rect.right = param.x + param.width;
                    rect.bottom = param.y + param.height;
                    mUndoLayoutParamsList.add(rect);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(NotePicture.this);
                    }

                   // Log.i("***", "ImageView ACTION_UP");
                    //Log.i("***", "param:x=" + param.x + ";y=" + param.y + ";w=" + param.width + ";h=" + param.height);
                    break;
            }
            return true;
        }
    }

    public class NotePictureCorner_Default extends ElementCorner {
        private int tmpX, tmpY;

        public NotePictureCorner_Default(Context context, int cornerType) {
            super(context, cornerType);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint defaultPaint = new Paint();
            defaultPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, mBoundSize - 1, mBoundSize - 1, defaultPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER) {
                return false;
            }

            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) NotePicture.this
                    .getLayoutParams();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tmpX = x;
                    tmpY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (cornerType == 0) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += x - tmpX;
                                param.width -= x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += y - tmpY;
                                param.height -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += y - tmpY;
                                param.width -= y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= x - tmpX;
                                param.height += x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= x - tmpX;
                                param.width += x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= y - tmpY;
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= y - tmpY;
                                param.width += y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += x - tmpX;
                                param.height -= x - tmpX;
                            }
                        }
                    } else if (cornerType == 1) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += y - tmpY;
                                param.height -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= x - tmpX;
                                param.height += x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= y - tmpY;
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += x - tmpX;
                                param.height -= x - tmpX;
                            }
                        }
                    } else if (cornerType == 2) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += x - tmpX;
                            }
                        }
                    } else if (cornerType == 3) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += x - tmpX;
                                param.width -= x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += y - tmpY;
                                param.width -= y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= x - tmpX;
                                param.width += x - tmpX;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= y - tmpY;
                                param.width += y - tmpY;
                            }
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += x - tmpX;
                            }
                        }
                    }

                    mFinalWidth = param.width;
                    mFinalHeight = param.height;
                    NotePicture.this.setLayoutParams(param);
                    invalidate();

                    tmpX = x;
                    tmpY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    Rect rect = new Rect();
                    rect.left = param.x;
                    rect.top = param.y;
                    rect.right = param.x + param.width;
                    rect.bottom = param.y + param.height;
                    mUndoLayoutParamsList.add(rect);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(NotePicture.this);
                    }
                    break;
            }
            return true;
        }
    }

    public class NotePictureBorder_Default extends ElementBorder {
        int tmpX, tmpY;

        public NotePictureBorder_Default(Context context, int borderType) {
            super(context, borderType);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Path path = NotePicture.this.getPath();
            if (mBorderType == 0 || mBorderType == 2) {

                path.moveTo(0, mBoundSize / 2);
                path.lineTo(mFinalWidth / 2 - mBoundSize / 2, mBoundSize / 2);
                canvas.drawRect(mFinalWidth / 2 - mBoundSize / 2,
                        0,
                        mFinalWidth / 2 + mBoundSize / 2,
                        mBoundSize,
                        NotePicture.this.getDefaultPaint());
                path.moveTo(mFinalWidth / 2 + mBoundSize / 2, mBoundSize / 2);
                path.lineTo(mFinalWidth, mBoundSize / 2);
            } else if (mBorderType == 1 || mBorderType == 3) {

                path.moveTo(mBoundSize / 2, 0);
                path.lineTo(mBoundSize / 2, mFinalHeight / 2 - mBoundSize / 2);

                canvas.drawRect(0,
                        mFinalHeight / 2 - mBoundSize / 2,
                        mBoundSize,
                        mFinalHeight / 2 + mBoundSize / 2,
                        NotePicture.this.getDefaultPaint());

                path.moveTo(mBoundSize / 2, mFinalHeight / 2 + mBoundSize / 2);
                path.lineTo(mBoundSize / 2, mFinalHeight);
            }

            canvas.drawPath(path, NotePicture.this.getLinePaint());
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER ) {
                return false;
            }

            int x = (int)event.getRawX();
            int y = (int)event.getRawY();
            AbsoluteLayout.LayoutParams param = (AbsoluteLayout.LayoutParams) NotePicture.this
                    .getLayoutParams();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tmpX = x;
                    tmpY = y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mBorderType == 0) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += y - tmpY;
                                param.height -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= x - tmpX;
                                param.height += x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y -= y - tmpY;
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.y += x - tmpX;
                                param.height -= x - tmpX;
                            }
                        }
                    } else if (mBorderType == 1) {
                        if(PageEditorActivity.canvasRotation == 0){

                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.width -= y - tmpY;
                            }
                        }
                    } else if (mBorderType == 2) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height -= y - tmpY;
                            }

                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.height < mMinHeight){
                                param.height = mMinHeight;
                            } else if(param.height > mMaxHeight){
                                param.height = mMaxHeight;
                            } else {
                                param.height += x - tmpX;
                            }
                        }
                    } else if (mBorderType == 3) {
                        if(PageEditorActivity.canvasRotation == 0){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += x - tmpX;
                                param.width -= x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 90){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x += y - tmpY;
                                param.width -= y - tmpY;
                            }
                        } else if(PageEditorActivity.canvasRotation == 180){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= x - tmpX;
                                param.width += x - tmpX;
                            }
                        } else if(PageEditorActivity.canvasRotation == 270){
                            if(param.width < mMinWidth){
                                param.width = mMinWidth;
                            } else if(param.width > mMaxWidth){
                                param.width = mMaxWidth;
                            } else {
                                param.x -= y - tmpY;
                                param.width += y - tmpY;
                            }
                        }
                    }

                    mFinalWidth = param.width;
                    mFinalHeight = param.height;
                    NotePicture.this.setLayoutParams(param);
                    invalidate();

                    tmpX = x;
                    tmpY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    Rect rect = new Rect();
                    rect.left = param.x;
                    rect.top = param.y;
                    rect.right = param.x + param.width;
                    rect.bottom = param.y + param.height;
                    mUndoLayoutParamsList.add(rect);
                    if (PageEditorActivity.getInstance() != null) {
                        PageEditorActivity.getInstance().addRecord(NotePicture.this);
                    }
                    break;
            }
            return true;
        }
    }

    @Override
    public View getContentView() {
        return mContentView;
    }
}

