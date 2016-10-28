package com.hanvon.virtualpage.common;


import com.hanvon.virtualpage.beans.Page;

import java.util.List;

/**
 * -------------------------------
 * Description:
 * <p/> 提供给滑动列表新的Page被聚焦的时候的回调监听类
 * -------------------------------
 * Author:  TaoZhi
 * Date:    2016/4/17
 * E_mail:  taozhi@hanwang.com.cn
 */

public interface OnFocusItemChangedListener {

    /**
     * 当聚焦到新的位置的时候调用此方法
     * @param newPosition 新的条目位置
     */
    void onNewItemFocused(int newPosition);

    /**
     * 当PageList中的内容发生变化的时候调用此方法
     * @param newList 新的数据集合
     */
    void onListContentChanged(List<Page> newList);


//    void onNewPositionClicked(int newPosition);
}
