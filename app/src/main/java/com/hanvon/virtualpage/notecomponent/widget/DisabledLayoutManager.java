package com.hanvon.virtualpage.notecomponent.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * @Description: 可以设定是否能滚动
 * @Author: TaoZhi
 * @Date: 2016/6/15
 * @E_mail: taozhi@hanwang.com.cn
 */
public class DisabledLayoutManager extends LinearLayoutManager {

    private boolean canScroll = true;

    public DisabledLayoutManager(Context context) {
        super(context);
    }

    public DisabledLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setScrollEnable(boolean canScroll) {
        this.canScroll = canScroll;
    }

    @Override
    public boolean canScrollVertically() {
        return super.canScrollVertically() && canScroll;
    }

    @Override
    public boolean canScrollHorizontally() {
        return super.canScrollHorizontally() && canScroll;
    }
}
