package com.hanvon.virtualpage.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.hanvon.virtualpage.pageeditor.tools.stroke.NoteParams;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

/**
 * -------------------------------
 * Description:
 * element(text,picture) layout, include borders and corners......
 * -------------------------------
 */
public abstract class ElementLayout extends RelativeLayout implements Observer {

    protected int mBoundSize = 20;
    protected int mBoundType = 0;
    protected ElementBorder[] mBorderList;
    protected ElementCorner[] mCornerList;

    protected ElementType mCurrentElementType = ElementType.STROKE;

    public LinkedList<Rect> mUndoLayoutParamsList = new LinkedList<Rect>();
    public LinkedList<Rect> mRedoLayoutParamsList = new LinkedList<Rect>();

    public ElementLayout(Context context) {
        super(context);
    }

    public ElementLayout(Context context, int boundSize) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
    }

    abstract public View getContentView();

    abstract public void onRemovedFromParent();

    protected void addBounds() {
        removeBounds();
        mBorderList = new ElementBorder[4];
        mCornerList = new ElementCorner[4];
    }

    protected abstract void addContentView();

    protected void removeBounds() {
        if (mBorderList != null && mBorderList.length > 0) {
            for (int i = 0; i < mBorderList.length; i++) {
                removeView(mBorderList[i]);
            }
        }
        if (mCornerList != null && mCornerList.length > 0) {
            for (int i = 0; i < mCornerList.length; i++) {
                removeView(mCornerList[i]);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (EditorState.getInstance().getStrokeType() == EditorState.PAINT_ERASER
                || NoteParams.getCurrentPenNoteParams().getCurrentElementType() == ElementType.STROKE) {
            return false;
        }
        if (getContentView().hasFocus() != true) {
            getContentView().requestFocus();
        }
        return true;
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof ElementType) {
            mCurrentElementType = (ElementType) data;
        }
    }

    public void setBoundSize(int boundSize) {
        this.mBoundSize = boundSize;
    }

    public int getBoundSize() {
        return mBoundSize;
    }

    public void setBoundType(int boundType) {
        this.mBoundType = boundType;
    }

    public int getBoundType() {
        return mBoundType;
    }

    public abstract class ElementBorder extends View {
        protected int mBorderType;

        public ElementBorder(Context context, int borderType) {
            super(context);

            if (borderType < 0) {
                this.mBorderType = 0;
            } else if (borderType > 3) {
                this.mBorderType = 3;
            } else {
                this.mBorderType = borderType;
            }

            init();
        }

        public void init() {
            RelativeLayout.LayoutParams rlLP;
            switch (mBorderType) {
                case 0:// top border
                    rlLP = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.FILL_PARENT, mBoundSize);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    rlLP.setMargins(mBoundSize, 0, mBoundSize, 0);
                    break;
                case 1:// right border
                    rlLP = new RelativeLayout.LayoutParams(mBoundSize,
                            ViewGroup.LayoutParams.FILL_PARENT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rlLP.setMargins(0, mBoundSize, 0, mBoundSize);
                    break;
                case 2:// bottom border
                    rlLP = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.FILL_PARENT, mBoundSize);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    rlLP.setMargins(mBoundSize, 0, mBoundSize, 0);
                    break;
                case 3:// left border
                default:
                    rlLP = new RelativeLayout.LayoutParams(mBoundSize,
                            ViewGroup.LayoutParams.FILL_PARENT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlLP.setMargins(0, mBoundSize, 0, mBoundSize);
                    break;
            }
            setLayoutParams(rlLP);
        }
    }

    public abstract class ElementCorner extends View {
        protected int cornerType;

        public ElementCorner(Context context, int cornerType) {
            super(context);

            if (cornerType < 0) {
                this.cornerType = 0;
            }
            else if (cornerType > 3) {
                this.cornerType = 3;
            }
            else {
                this.cornerType = cornerType;
            }

            init();
        }

        public void init() {
            RelativeLayout.LayoutParams rlLP = new RelativeLayout.LayoutParams(
                    mBoundSize, mBoundSize);
            switch (cornerType) {
                case 0:// left top
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    break;
                case 1:// right top
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    break;
                case 2:// right bottom
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
                case 3:// left bottom
                default:
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rlLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
            }
            setLayoutParams(rlLP);
        }
    }
}