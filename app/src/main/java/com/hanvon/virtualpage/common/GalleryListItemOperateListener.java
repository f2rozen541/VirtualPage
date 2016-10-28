package com.hanvon.virtualpage.common;

import com.hanvon.virtualpage.beans.Page;

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

public interface GalleryListItemOperateListener {

    /**
     * 条目被点击时候的回调
     * @param position 被点击条目的位置
     */
    void onPageItemClick(int position);

    /**
     * 条目被长按时候的回调
     * @param position 被点击条目的位置
     */
    void onPageItemLongClick(int position);

    /**
     * 条目被拖动，内容改变后的回调
     * @param newList 内容改变后的数据
     */
    void onPageDraggedOver(List<Page> newList);
}
