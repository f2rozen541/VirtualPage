package com.hanvon.virtualpage.notecomponent.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hanvon.virtualpage.BaseApplication;
import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.beans.Page;
import com.hanvon.virtualpage.beans.UIConstants;

import java.util.ArrayList;
import java.util.List;


public class PageWidgetAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater inflater;
    private Page mCoverPage;

    private List<Page> mPagesInfo;

    public PageWidgetAdapter(Context context) {
        mContext = context;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPagesInfo = new ArrayList<>();
    }

    public void setCoverPage(Page coverPage) {
        this.mCoverPage = coverPage;
    }

    public Page getCoverPage() {
        return mPagesInfo.size() > 0 ? mPagesInfo.get(0) : null;
    }

    public void setData(List<Page> mData) {
        mPagesInfo = mData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPagesInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mPagesInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup widgetItem;
        if (convertView == null) {
            widgetItem = (ViewGroup) inflater.inflate(R.layout.page_in_note_item, null);
        } else {
            widgetItem = (ViewGroup) convertView;
        }
        setViewContent(widgetItem, position);
        return widgetItem;
    }

    private void setViewContent(ViewGroup group, int position) {
        ImageView right = (ImageView) group.findViewById(R.id.fl_right_page);
        ImageView left = (ImageView) group.findViewById(R.id.fl_left_page);

        if (position < 0 || position >= mPagesInfo.size()) {
            return;
        }

        if (position == 0) {
            left.setVisibility(View.INVISIBLE);
            right.setImageResource(Integer.parseInt(mPagesInfo.get(position).getBackgroundResIndex())); // 设置封面
            right.setBackground(null);
        } else if (position == 1) {
            left.setVisibility(View.INVISIBLE);
            Glide.with(BaseApplication.getContext())
                    .load(mPagesInfo.get(position).getThumbnailFilePath())
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .skipMemoryCache(true)
                    .into(right);
            int rotation = BaseApplication.getWindowManager().getDefaultDisplay().getRotation();
            //cuishuo1 delete
            //保存缩略图时按纸张方向保存，所以显示时不需要再调整方向
//            if (rotation == Surface.ROTATION_0) {
//                right.setRotation(180);
//            }
            int bgResIndex = UIConstants.getAvailableBgResIndex(mPagesInfo.get(position).getBackgroundResIndex());
            right.setBackgroundResource(UIConstants.ARRAY_PAGE_BG_REPEAT[bgResIndex]);
        } else {
            left.setVisibility(View.VISIBLE);
            right.setBackgroundResource(R.drawable.bg_page_open_right); // 设置page的背景图片
            Glide.with(BaseApplication.getContext())
                    .load(mPagesInfo.get(position).getThumbnailFilePath())
//                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                    .skipMemoryCache(true)
                    .into(right);
            int rotation = BaseApplication.getWindowManager().getDefaultDisplay().getRotation();
            //cuishuo1 delete
            //保存缩略图时按纸张方向保存，所以显示时不需要再调整方向
//            if (rotation == Surface.ROTATION_0) {
//                right.setRotation(180);
//            }
            int bgResIndex = UIConstants.getAvailableBgResIndex(mPagesInfo.get(position).getBackgroundResIndex());
            right.setBackgroundResource(UIConstants.ARRAY_PAGE_BG_REPEAT[bgResIndex]);
        }
    }

}
