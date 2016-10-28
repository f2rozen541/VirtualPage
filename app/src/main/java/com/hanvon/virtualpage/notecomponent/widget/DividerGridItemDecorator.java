package com.hanvon.virtualpage.notecomponent.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.hanvon.virtualpage.R;

/**
 * -------------------------------
 * Description:
 * <p/> RecyclerView分割线类
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/6/30
 * E_mail:  taozhi@hanwang.com.cn
 */
public class DividerGridItemDecorator extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[] {
            android.R.attr.listDivider
    };
    private boolean includeEdge;
    private Drawable mDivider;
    private int mColumnSpace;
    private int mRowSpace;

    public DividerGridItemDecorator(Context context) {
        super();
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.style.DefinedDivider});
        mDivider = typedArray.getDrawable(0);
        typedArray.recycle();
//        mDivider = context.getDrawable(R.drawable.bg_divider_transparent);
    }

    public DividerGridItemDecorator(Context context, int columnSpace, int rowSpace, boolean includeEdge) {
        this.mColumnSpace = columnSpace;
        this.mRowSpace = rowSpace;
        this.includeEdge = includeEdge;
    }

//    @Override
//    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDraw(c, parent, state);
//        drawHorizontalDivider(c, parent);
//        drawVerticalDivider(c, parent);
//    }

//    @Override
//    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
//        super.onDrawOver(c, parent, state);
//    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);
        int spanCount = getSpanCount(parent);
        int childCount = parent.getAdapter().getItemCount();
        int itemPosition = parent.getChildAdapterPosition(view);

        int position = parent.getChildAdapterPosition(view); // item position
        int column = position % spanCount; // item column
//        int spacing = mColumnSpace;

        // 这里的计算方式是将间隙值按照item位置设置到两边，以保证每两个item之间的间隙值之和相等
        if (includeEdge) {
            outRect.left = mColumnSpace - column * mColumnSpace / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * mColumnSpace / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
            outRect.top = 0;
            outRect.bottom = mRowSpace; // item bottom
        } else {
            outRect.left = column * mColumnSpace / spanCount; // column * ((1f / spanCount) * spacing)
            outRect.right = mColumnSpace - (column + 1) * mColumnSpace / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            outRect.top = 0;
            outRect.bottom = mRowSpace;
        }
//        int index = (itemPosition + 1) % spanCount;
//        outRect.top = 0;
//        outRect.left = 0;
//        if (index == 0) {
//            outRect.right = 0;
//        } else {
//            outRect.right = mColumnSpace;
//        }
//        outRect.bottom = mRowSpace;
//        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = view.getMeasuredWidth() + outRect.right + outRect.left;
//        layoutParams.height = view.getMeasuredHeight() + outRect.bottom + outRect.top;
//        view.setLayoutParams(layoutParams);

//        if (isLastRaw(parent, itemPosition, spanCount, childCount)) { // 如果是最后一行，则不需要绘制底部
//            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
//        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) { // 如果是最后一列，则不需要绘制右边
//            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
//        } else {
//            outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
//        }
    }

    private int getSpanCount(RecyclerView parent) {
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            spanCount = ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }
//
//    private void drawVerticalDivider(Canvas c, RecyclerView parent) {
//        int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View childView = parent.getChildAt(i);
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
//            int top = childView.getTop() - layoutParams.topMargin;
//            int bottom = childView.getBottom() + layoutParams.bottomMargin;
//            int left = childView.getRight() + layoutParams.rightMargin;
//            int right = left + mDivider.getIntrinsicWidth();
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
//        }
//
//    }
//
//    private void drawHorizontalDivider(Canvas c, RecyclerView parent) {
//        int childCount = parent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View childView = parent.getChildAt(i);
//            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) childView.getLayoutParams();
//            int left = childView.getLeft() - layoutParams.leftMargin;
//            int right = childView.getRight() - layoutParams.rightMargin;
//            int top = childView.getBottom() + layoutParams.bottomMargin;
//            int bottom = top + mDivider.getIntrinsicHeight();
//            mDivider.setBounds(left, top, right, bottom);
//            mDivider.draw(c);
//        }
//    }
//
//    private boolean isLastColum(RecyclerView parent, int pos, int spanCount, int childCount) {
//        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
//        if (layoutManager instanceof GridLayoutManager) {
//            if ((pos + 1) % spanCount == 0) { // 如果是最后一列，则不需要绘制右边
//                return true;
//            }
//        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
//            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
//            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
//                if ((pos + 1) % spanCount == 0) { // 如果是最后一列，则不需要绘制右边
//                    return true;
//                } else {
//                    childCount = childCount - childCount % spanCount;
//                    if (pos >= childCount) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount, int childCount)
//    {
//        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
//        if (layoutManager instanceof GridLayoutManager) {
//            childCount = childCount - childCount % spanCount;
//            if (pos >= childCount) {// 如果是最后一行，则不需要绘制底部
//                return true;
//            }
//        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
//            int orientation = ((StaggeredGridLayoutManager) layoutManager).getOrientation();
//            // StaggeredGridLayoutManager 且纵向滚动
//            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
//                childCount = childCount - childCount % spanCount;
//                // 如果是最后一行，则不需要绘制底部
//                if (pos >= childCount) {
//                    return true;
//                }
//            } else {// StaggeredGridLayoutManager 且横向滚动
//                // 如果是最后一行，则不需要绘制底部
//                if ((pos + 1) % spanCount == 0) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }


}
