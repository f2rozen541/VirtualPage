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
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * -------------------------------
 * Description:
 * <p/> 水平方向的画廊控件
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/3/21
 * E_mail:  taozhi@hanwang.com.cn
 */
public class HorizontalPageList extends FrameLayout {
    private Context mContext;
    private View parentView;
    private RecyclerView rvPageList;
    private PageListAdapter mPageListAdapter;
    private int mStepWidth;
    private int mParentWidth;
    private int mFocusView_X_Left;
    private int mFocusPosition;
    private View mFocusView;
    //    private int mFocusView_X_Right;
    private View fl_Focus;
    private View mHeaderView;
    private View mTailView;

    private OnFocusItemChangedListener mListener;
    private ItemTouchHelper mItemTouchHelper;

    private static final String TAG = "tz";
    private boolean needRelayout = true;
    private TextView tvFlag;
    private List<Page> mPageList;
    private PageItemTouchHelperCallBack pageItemTouchHelperCallBack;
    private DisabledLayoutManager layoutManager;

    public HorizontalPageList(Context context) {
        this(context, null);
    }

    public HorizontalPageList(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalPageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        parentView = LayoutInflater.from(mContext).inflate(R.layout.layout_portable_page_list, this);
        initViews();
        initData();
    }

    private void initViews() {
        rvPageList = (RecyclerView) parentView.findViewById(R.id.hv_page_list);
        fl_Focus = parentView.findViewById(R.id.fl_focus);
        tvFlag = (TextView) parentView.findViewById(R.id.tv_flag);
        tvFlag.setVisibility(GONE);

        layoutManager = new DisabledLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setScrollEnable(true);
        rvPageList.setLayoutManager(layoutManager);
        rvPageList.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        if (mHeaderView == null) mHeaderView = generateView();
        if (mTailView == null) mTailView = generateView();

    }

    private void initData() {
        needRelayout = true;
        if (mPageList == null) { // 如果当前数据为控，就设置默认数据
            mPageList = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                mPageList.add(new Page());
            }
        }
        mPageListAdapter = new PageListAdapter(mContext, mPageList, R.layout.item_portable_page_list);
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
                checkFocusByChild(); // 内容改变之后重新检查焦点
                mPageList = newList;
                if (mListener != null) {
                    Logger.t("onPageDraggedOver").i("当前的滑动状态为：" + rvPageList.getScrollState());
                    if (rvPageList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) { // 拖拽的时候滑动调用下面的方法会FC的问题
                        mListener.onListContentChanged(newList);
                    }
                }
//                setScrollEnableState(true);
            }
        });
        mPageListAdapter.setHeaderView(mHeaderView);
        mPageListAdapter.setTailView(mTailView);
        rvPageList.setAdapter(mPageListAdapter); // 设置Adapter
        pageItemTouchHelperCallBack = new PageItemTouchHelperCallBack(mPageListAdapter);
        mItemTouchHelper = new ItemTouchHelper(pageItemTouchHelperCallBack);
        mItemTouchHelper.attachToRecyclerView(rvPageList);
        rvPageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkFocusByChild();
                    tvFlag.setText("" + mFocusPosition);
                    if (mListener != null) {
                        mListener.onNewItemFocused(mFocusPosition);
                    }
                }
            }
        });
        mFocusView = rvPageList.getChildAt(1);
        mFocusPosition = 1;
    }
    /**
     * 设置控件是否可以滚动
     * @param canScroll
     */
    public void setScrollEnableState(boolean canScroll) {
        layoutManager.setScrollEnable(canScroll);
    }


//    public PageListAdapter getPageListAdapter() {
//        return mPageListAdapter;
//    }


    /**
     * 生成一个头布局
     *
     * @return
     */
    private View generateView() {
        View view = new View(mContext);
        view.setLayoutParams(new ViewGroup.LayoutParams(10, 10)); // 随便初始化一个宽高，占据位置，在layout中会重新设置宽高
        return view;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (needRelayout) {
            needRelayout = false;
            mParentWidth = rvPageList.getWidth();
            if (rvPageList.getChildAt(1) != null) { // 获取第二个子条目的宽度
                mStepWidth = rvPageList.getChildAt(1).getWidth();
            }
            mFocusView_X_Left = fl_Focus.getLeft();

            ViewGroup.LayoutParams lp_HeaderView = mHeaderView.getLayoutParams();
            lp_HeaderView.width = mFocusView_X_Left;
            mHeaderView.setLayoutParams(lp_HeaderView);

            ViewGroup.LayoutParams lp_TailView = mTailView.getLayoutParams();
            lp_TailView.width = mParentWidth - mFocusView_X_Left - mStepWidth;
            mTailView.setLayoutParams(lp_TailView);
            requestLayout();
        }
    }

    /**
     * 根据当前HorizontalView中的控件位置，设置当前应该被聚焦的条目
     */
    public void checkFocusByChild() {
        int i = 1;
        View child = rvPageList.getChildAt(i);
        while (child != null) {
            int gapX = mFocusView_X_Left - child.getLeft(); // 计算焦点框与item之间的距离
            if (gapX <= mStepWidth) { // 找到最近的page
                if (gapX < mStepWidth / 2) { // 如果超过一半在焦点框内，就向右滑动
                    rvPageList.smoothScrollBy(-gapX, 0);
                    mFocusView = child;
                } else { // 否则，就向左滑动
                    rvPageList.smoothScrollBy(mStepWidth - gapX, 0);
                    mFocusView = rvPageList.getChildAt(i + 1);
                }
                break;
            }
            i++;
            child = rvPageList.getChildAt(i);
        }
        mFocusPosition = rvPageList.getChildAdapterPosition(mFocusView);
    }

    /**
     * 设置需要被显示的数据，实际上是设置给Adapter
     *
     * @param dataList
     */
    public void setPageListData(List<Page> dataList) {
        this.mPageList = dataList;
        mPageListAdapter.setData(mPageList);
        if (mFocusPosition > mPageList.size()) { // 删除数据之后，旧的数据可能还没有被修改，导致错误的计算
            mFocusPosition = mPageList.size();
        }
    }

    /**
     * 直接滑动到指定位置,并调用被点击的事件回调方法
     *
     * @param newPosition 新指定的位置
     */
    public void moveToSpecificPosition(int newPosition) {
//        if (mFocusPosition > mPageList.size()) { // 删除数据之后，就的数据可能还没有被修改，导致错误的计算
//            mFocusPosition = mPageList.size();
//        }
        int gapSteps = newPosition - mFocusPosition;
        if (gapSteps == 0) {
           return;
        }
        if (gapSteps > 10) {
            rvPageList.scrollBy((gapSteps - 10) * mStepWidth, 0);
            gapSteps = 10;
        }
        rvPageList.smoothScrollBy(gapSteps * mStepWidth, 0);
    }


    public int getFocusPosition() {
        return mFocusPosition;
    }

    public void setFocusPosition(int position) {
        mFocusPosition = position;
        tvFlag.setText(mFocusPosition + "");
    }

    public void setOnFocusChangedListener(OnFocusItemChangedListener listener) {
        this.mListener = listener;
    }


}
