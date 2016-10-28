package com.hanvon.virtualpage.pageeditor.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.hanvon.virtualpage.R;
import com.hanvon.virtualpage.common.GalleryListView;

/**
 * -------------------------------
 * Description:
 * <p/>
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/7/10
 * E_mail:  taozhi@hanwang.com.cn
 */
public class LandscapePageList extends GalleryListView {

    public LandscapePageList(Context context) {
        super(context);
    }

    public LandscapePageList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LandscapePageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.layout_landscape_page_list;
    }

    @Override
    public int getItemLayoutResource() {
        return R.layout.item_landscape_page_list;
    }

//    @Override
//    public void setData(List<Page> pageList) {
//        super.setData(pageList);
//    }
}
