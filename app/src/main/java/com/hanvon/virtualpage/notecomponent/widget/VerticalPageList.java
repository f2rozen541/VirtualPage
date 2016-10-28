package com.hanvon.virtualpage.notecomponent.widget;


import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.notecomponent.adapter.PageListAdapter;
import com.hanvon.virtualpage.notecomponent.adapter.helper.PageItemTouchHelperCallBack;
import com.hanvon.virtualpage.common.OnFocusItemChangedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/> 竖直方向的画廊控件
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/4/16
 * E_mail:  taozhi@hanwang.com.cn
 */
public class VerticalPageList extends FrameLayout {

    private Context mContext;
    private View parentView;
    private RecyclerView rvPageList;
    private FrameLayout flFocus;
    private TextView tvFlag;
    private List<Page> mPageList;
    private boolean needRelayout;
    private PageListAdapter mPageListAdapter;
    private View mHeadView;
    private View mTailView;
    private int mWidth;
    private int mHeight;
    private int mFocusView_top_y;
    private int mStepHeight;
    private int mFocusPosition;
    private View mCurrView;
    private OnFocusItemChangedListener mListener;
    private ItemTouchHelper.Callback mPageItemTouchHelperCallBack;
    private ItemTouchHelper mItemTouchHelper;
    private DisabledLayoutManager layoutManager;

    public VerticalPageList(Context context) {
        this(context, null);
    }

    public VerticalPageList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalPageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        parentView = LayoutInflater.from(mContext).inflate(R.layout.vertical_page_list, this);
        initViews();
        initData();
    }

    private void initViews() {
        rvPageList = (RecyclerView) parentView.findViewById(R.id.rv_page_list);
        flFocus = (FrameLayout) parentView.findViewById(R.id.fl_focus);
        tvFlag = (TextView) parentView.findViewById(R.id.tv_flag);
        tvFlag.setVisibility(GONE);

        layoutManager = new DisabledLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvPageList.setLayoutManager(layoutManager);
        rvPageList.setOverScrollMode(OVER_SCROLL_NEVER);

        if (mHeadView == null) {
            mHeadView = generateNewView();
        }
        if (mTailView == null){
            mTailView = generateNewView();
        }
    }

    private void initData() {
        needRelayout = true;
        mFocusPosition = 1;
        if (mPageList == null) {
            mPageList = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                mPageList.add(new Page());
            }
        }
        mPageListAdapter = new PageListAdapter(mContext, mPageList, R.layout.vertical_page_list_item);
        mPageListAdapter.setHeaderView(mHeadView);
        mPageListAdapter.setTailView(mTailView);
        mPageListAdapter.setOnPageItemOperateListener(new PageListAdapter.OnPageItemOperateListener() {
            @Override
            public void onPageItemClick(int position) {
                moveToSpecificPosition(position);
            }

            @Override
            public void onPageItemLongClick(int position) {
//                Logger.i("item被长按了，RecyclerView无法滑动");
//                setScrollEnableState(false);
            }

            @Override
            public void onPageDraggedOver(List<Page> newList) {
//                Logger.i("onPageDraggedOver方法被调用了");
//                setScrollEnableState(true);
                checkFocusByChild(); // 内容改变之后重新检查焦点
                mPageList = newList;
                if (mListener != null) {
                    mListener.onListContentChanged(newList);
                }
            }
        });
        rvPageList.setAdapter(mPageListAdapter);
        mPageItemTouchHelperCallBack = new PageItemTouchHelperCallBack(mPageListAdapter);
        mItemTouchHelper = new ItemTouchHelper(mPageItemTouchHelperCallBack);
        mItemTouchHelper.attachToRecyclerView(rvPageList);

        rvPageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkFocusByChild();
//                    LogUtil.d("VerticalPageList:-----checkFocusByChild()------>" + mFocusPosition);
                    tvFlag.setText(mFocusPosition + "");
                    if (mListener != null) {
                        mListener.onNewItemFocused(mFocusPosition);
                    }
                }
            }

        });
    }

    private void checkFocusByChild() {
        int childPosition = 1;
        View child = rvPageList.getChildAt(childPosition);
        int gapY;
        while (child != null){
            gapY = mFocusView_top_y - child.getTop();
            if(gapY <= mStepHeight){
                if (gapY <= mStepHeight / 2) { // 向上滑动，当前
                    rvPageList.smoothScrollBy(0, -gapY);
                    mCurrView = child;
                } else { // 向下滑动，下一个
                    rvPageList.smoothScrollBy(0, mStepHeight - gapY);
                    mCurrView = rvPageList.getChildAt(childPosition + 1);
                }
                break;
            }
            childPosition++;
            child = rvPageList.getChildAt(childPosition);
        }
        mFocusPosition = rvPageList.getChildAdapterPosition(mCurrView);
    }

    public void setPageListData(List<Page> pageList){
        mPageList = pageList;
        mPageListAdapter.setData(mPageList);
        if (mFocusPosition > mPageList.size()) { // 删除数据之后，就的数据可能还没有被修改，导致错误的计算
            mFocusPosition = mPageList.size();
        }

    }

    public void moveToSpecificPosition(int newPosition){
//        if (mFocusPosition > mPageList.size()) { // 删除数据之后，就的数据可能还没有被修改，导致错误的计算
//            mFocusPosition = mPageList.size();
//        }
        int gapSteps = newPosition - mFocusPosition;
        if (gapSteps == 0) {
            return;
        }
        if (gapSteps > 10) {
            rvPageList.scrollBy(0, (gapSteps - 10) * mStepHeight);
            gapSteps = 10;
        }
        rvPageList.smoothScrollBy(0, gapSteps * mStepHeight);
        setFocusPosition(newPosition);
    }


    private View generateNewView() {
        View view = new View(mContext);
        view.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
        return view;
    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        needRelayout = true;
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (needRelayout) {
            needRelayout = false;
            mWidth = rvPageList.getWidth();
            mHeight = rvPageList.getHeight();
            mFocusView_top_y = flFocus.getTop();
            if(rvPageList.getChildAt(1) != null){
                mStepHeight = rvPageList.getChildAt(1).getHeight();
            }

            ViewGroup.LayoutParams lpHead = mHeadView.getLayoutParams();
            lpHead.height = mFocusView_top_y;
            mHeadView.setLayoutParams(lpHead);
            ViewGroup.LayoutParams lpTail = mTailView.getLayoutParams();
            lpTail.height = mHeight - mFocusView_top_y - mStepHeight;
            mTailView.setLayoutParams(lpTail);

            requestLayout();
        }
    }

//    @Override
//    protected void onVisibilityChanged(View changedView, int visibility) {
//        super.onVisibilityChanged(changedView, visibility);
//        if(visibility == VISIBLE){
//            LogUtil.d("VerticalPageList:----------->onVisibilityChanged:" + mFocusPosition);
//            rvPageList.scrollTo(0, 0);
//            if(mFocusPosition > 1){
//                rvPageList.scrollBy(0, mStepHeight * (mFocusPosition - 1));
//            }
//        }
//    }


    public int getFocusPosition() {
        return mFocusPosition;
    }

    public void setFocusPosition(int position){
//        moveToSpecificPosition(position);
        mFocusPosition = position;
        tvFlag.setText(mFocusPosition + "");
    }

    public void setOnFocusChangedListener(OnFocusItemChangedListener listener){
        this.mListener = listener;
    }
}
