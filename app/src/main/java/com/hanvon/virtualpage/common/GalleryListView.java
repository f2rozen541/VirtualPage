package com.hanvon.virtualpage.common;

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
import com.hanvon.virtualpage.notecomponent.adapter.helper.PageItemTouchHelperCallBack;
import com.hanvon.virtualpage.notecomponent.widget.DisabledLayoutManager;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/> 画廊控件
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/14
 * E_mail:  taozhi@hanwang.com.cn
 */
public abstract class GalleryListView extends FrameLayout implements GalleryListItemOperateListener {

    private int layoutResId = -1;
    private int layoutItemResId = -1;
    private Context mContext;
    private View mParentView;
    private RecyclerView rvPageList;
//    private LandscapeListAdapter mPageListAdapter;
    private GalleryListViewAdapter mPageListAdapter;
    private DisabledLayoutManager layoutManager;
    private PageItemTouchHelperCallBack pageItemTouchHelperCallBack;
    private ItemTouchHelper mItemTouchHelper;
    private OnFocusItemChangedListener mListener;

    private int mStepWidth;
    private int mParentWidth;
    private int mFocusView_X_Left;
    private int mFocusPosition;
    private View mFocusView;
    private View fl_Focus;
    private View mHeaderView;
    private View mTailView;
    private TextView tvFlag;
    private boolean needRelayout = true;
    private List<Page> mPageList;

    public GalleryListView(Context context) {
        super(context);
        mContext = context;
        initViews();
        initData();
    }

    public GalleryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        initData();
    }

    public GalleryListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initViews();
        initData();
    }

    private void initViews() {
        layoutResId = getLayoutResource();
        if (layoutResId < 0) {
            throw new IllegalArgumentException("You have passed an invalid resource ID");
        }
        mParentView = LayoutInflater.from(getContext()).inflate(layoutResId, this);
        rvPageList = (RecyclerView) mParentView.findViewById(R.id.hv_page_list);
        fl_Focus = mParentView.findViewById(R.id.fl_focus);
        tvFlag = (TextView) mParentView.findViewById(R.id.tv_flag);
        tvFlag.setVisibility(GONE);

        layoutManager = new DisabledLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setScrollEnable(true);
        rvPageList.setLayoutManager(layoutManager);
        rvPageList.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        if (mHeaderView == null) mHeaderView = generateView();
        if (mTailView == null) mTailView = generateView();

    }

    /**
     * 将要被加载的布局资源
     * @return 将被加载的布局资源ID
     */
    public abstract int getLayoutResource();

    /**
     * 将要被加载到控件中的条目的布局
     * @return 条目的布局资源ID
     */
    public abstract int getItemLayoutResource();

//    /**
//     * 将要被显示的数据
//     * @return 数据list
//     */
//    public abstract List<Page> getPageListData();

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

    public void setPageListAdapter(GalleryListViewAdapter pageListAdapter) {
        mPageListAdapter = pageListAdapter;
    }

//    public void setData(List<Page> pageList) {
//        mPageList = pageList;
//        mPageListAdapter.setData(pageList);
////        initData();
//    }

    private void initData() {
        needRelayout = true;
        if (mPageList == null) { // 如果当前数据为控，就设置默认数据
            mPageList = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                mPageList.add(new Page());
            }
        }
        layoutItemResId = getItemLayoutResource();
        if (layoutItemResId < 0) {
            throw new IllegalArgumentException("You have passed an invalid resource ID");
        }
        mPageListAdapter = new GalleryListViewAdapter(mContext, mPageList);
//        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mPageListAdapter = new LandscapeListAdapter(mContext, mPageList, layoutItemResId);
//        } else {
//            mPageListAdapter = new PortableListAdapter(mContext, mPageList, layoutItemResId);
//        }
        mPageListAdapter.setOnPageItemOperateListener(this);
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
     * 直接滑动到指定位置,并调用被点击的事件回调方法
     *
     * @param newPosition 新指定的位置
     */
    public void moveToSpecificPosition(int newPosition) {
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


    /** 条目点击相关的回调方法 **/
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
        checkFocusByChild(); // 内容改变之后重新检查焦点
        mPageList = newList;
        Logger.t("onPageDraggedOver").i("onPageDraggedOver方法被调用了===>" + mFocusPosition);
        if (mListener != null) {
//            Logger.t("onPageDraggedOver").i("当前的滑动状态为：" + rvPageList.getScrollState());
            if (rvPageList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                mListener.onListContentChanged(newList);
            }
        }
    }

}
