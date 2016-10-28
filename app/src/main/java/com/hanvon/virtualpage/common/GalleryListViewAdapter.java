package com.hanvon.virtualpage.common;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;
import com.hanvon.virtualpage.notecomponent.adapter.helper.ItemTouchHelperAdapter;
import com.hanvon.virtualpage.notecomponent.adapter.helper.ItemTouchHelperViewHolder;
import com.hanvon.virtualpage.utils.LogUtil;

import java.util.Collections;
import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/14
 * E_mail:  taozhi@hanwang.com.cn
 */
public class GalleryListViewAdapter extends RecyclerView.Adapter implements ItemTouchHelperAdapter {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_HEADER = 1;
    public static final int Type_TAILER = 2;

    private View mHeaderView;
    private View mTailView;

    private Context mContext;
    private List<Page> mPageInfoList;
    private GalleryListItemOperateListener mListener;

    private int mItemLayoutResId = -1;
    private int mViewPosition = -1;


    public GalleryListViewAdapter(Context mContext) {
        this.mContext = mContext;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mItemLayoutResId = R.layout.item_landscape_page_list;
        } else {
            mItemLayoutResId = R.layout.item_portable_page_list;
        }
//        this.mPageInfoList = getPageListData();
//        this.mItemLayoutResId = getItemLayoutId();
    }

    public GalleryListViewAdapter(Context context, List<Page> pageInfoList) {
        this.mContext = context;
        this.mPageInfoList = pageInfoList;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mItemLayoutResId = R.layout.item_landscape_page_list;
        } else {
            mItemLayoutResId = R.layout.item_portable_page_list;
        }
//        this.mItemLayoutResId = getItemLayoutId();
    }
    public GalleryListViewAdapter(Context context, List<Page> pageInfoList, int itemLayoutResId) {
        this.mContext = context;
        this.mPageInfoList = pageInfoList;
        this.mItemLayoutResId = itemLayoutResId;
    }

    public void setData(List<Page> dataList){
        this.mPageInfoList = dataList;
        notifyDataSetChanged();
    }

    public List<Page> getPageInfoList() {
        return mPageInfoList;
    }

    public void setItemLayoutResId(int itemLayoutResId) {
        this.mItemLayoutResId = itemLayoutResId;
    }

//    public abstract int getItemLayoutId();
//
//    public abstract List<Page> getPageListData();

    public void setHeaderView(View view){
        mHeaderView = view;
        notifyItemInserted(0);
    }
    public View getHeaderView(){
        return mHeaderView;
    }

    public void setTailView(View view){
        mTailView = view;
        notifyItemInserted(getItemCount());
    }
    public View getTailView(){
        return mTailView;
    }

    @Override
    public int getItemViewType(int position) {
        if(mHeaderView == null && mTailView == null){
            return TYPE_NORMAL;
        }
        if(mHeaderView != null && position == 0){
            return TYPE_HEADER;
        }
        if(mTailView != null && position == (getItemCount() - 1)){
            return Type_TAILER;
        }
        return TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER){
            return new PageViewHolder(mHeaderView);
        }
        if(mTailView != null && viewType == Type_TAILER){
            return new PageViewHolder(mTailView);
        }

        View inflate = LayoutInflater.from(mContext).inflate(mItemLayoutResId, parent, false);
        return new PageViewHolder(inflate);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == TYPE_HEADER || getItemViewType(position) == Type_TAILER){
            return;
        }
        int realDataPosition = getRealDataPosition(holder);
        PageViewHolder pageViewHolder = (PageViewHolder) holder;
        // TODO: 2016/3/21 这里真实加载缩略图路径图片
        Page pageInfo = getPageInfoList().get(realDataPosition);
        if (pageInfo.getOwner() != null) {
            if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // 横屏加载横屏图片和布局
                ImageView pageThumbnail = pageViewHolder.pageThumbnail;
                Glide.with(BaseApplication.getContext())
                        .load(pageInfo.getThumbnailFilePath())
                        .transform(new RotateTransformation(mContext, 90))
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .skipMemoryCache(true)
                        .into(pageThumbnail);
                int bgResIndex = UIConstants.getAvailableBgResIndex(pageInfo.getBackgroundResIndex());
                pageThumbnail.setBackgroundResource(UIConstants.ARRAY_PAGE_BG_REPEAT[bgResIndex]);
                pageViewHolder.currentIndex = position;

            } else {
                // 竖屏加载竖屏图片和布局
                ImageView pageThumbnail = pageViewHolder.pageThumbnail;
                Glide.with(BaseApplication.getContext())
                        .load(pageInfo.getThumbnailFilePath())
//                        .diskCacheStrategy(DiskCacheStrategy.NONE)
//                        .skipMemoryCache(true)
                        .into(pageThumbnail);
                int rotation = BaseApplication.getWindowManager().getDefaultDisplay().getRotation();
                //cuishuo1 delete
                //保存缩略图时按纸张方向保存，所以显示时不需要再调整方向
//                if (rotation == Surface.ROTATION_0) {
//                    pageThumbnail.setRotation(180);
//                }
                int bgResIndex = UIConstants.getAvailableBgResIndex(pageInfo.getBackgroundResIndex());
                pageThumbnail.setBackgroundResource(UIConstants.ARRAY_PAGE_BG_REPEAT[bgResIndex]);
                pageViewHolder.currentIndex = position;
            }

        } else {
            LogUtil.e("bmp", "没人要的孩子没人管，看不到咯");
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = mPageInfoList.size();
        if(mHeaderView != null){
            itemCount++;
        }
        if(mTailView != null){
            itemCount++;
        }
        return itemCount;
    }

    /**
     * 获取当前holder真正对应的数据位置，如果有头布局，实际数据位置可能需要-1
     * @param holder  需要获取数据的holder
     * @return 对应的数据位置
     */
    public int getRealDataPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        if(mHeaderView != null){
            position--;
        }
        return position;
    }
    /**
     * 获取当前的选中的视图位置
     * @return 当前选中的视图位置
     */
    public int getViewPosition() {
        return mViewPosition;
    }

    public void setViewPosition(int newPosition){
        this.mViewPosition = newPosition;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        int fromRealData = fromPosition;
        int toRealData = toPosition;
        if (mHeaderView != null) {
            fromRealData = fromPosition - 1;
            toRealData = toPosition - 1;
        }
        Collections.swap(mPageInfoList, fromRealData, toRealData);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }


    @Override
    public void onItemDismiss(int position) {
        // do nothing
    }

    @Override
    public void onItemClear() {
        if (mListener != null) {
            mListener.onPageDraggedOver(mPageInfoList);
        }
    }

    public class PageViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, View.OnClickListener {

        public PageViewHolder(View itemView) {
            super(itemView);
            if(itemView == mHeaderView || itemView == mTailView){
                return;
            }
            pageThumbnail = (ImageView) itemView.findViewById(R.id.iv_page_item);
            pageThumbnail.setOnClickListener(this);
        }

        public ImageView pageThumbnail;
        public int currentIndex;

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onPageItemClick(currentIndex);
            }
        }
    }

    public void setOnPageItemOperateListener(GalleryListItemOperateListener listener){
        this.mListener = listener;
    }

}
