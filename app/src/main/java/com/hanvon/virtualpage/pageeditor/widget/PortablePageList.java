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
 * Date:    2016/7/14
 * E_mail:  taozhi@hanwang.com.cn
 */
public class PortablePageList extends GalleryListView {

    public PortablePageList(Context context) {
        super(context);
    }

    public PortablePageList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortablePageList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutResource() {
        return R.layout.layout_portable_page_list;
    }

    @Override
    public int getItemLayoutResource() {
        return R.layout.item_portable_page_list;
    }

//    @Override
//    public void setData(List<Page> pageList) {
//        super.setData(pageList);
//    }

}
