package com.hanvon.virtualpage.common;

import android.widget.AbsoluteLayout;

/**
 * -------------------------------
 * Description:
 * element对象接口，提供设置对象的布局等信息
 * ------------------------------
 */
public interface IElementBuilder {
    void setElementLayoutParams(AbsoluteLayout.LayoutParams params);

    void setElementHorizontalScrollBarEnabled(boolean flag);

    void buildBounds();

    void buildContentView();

    void setElementTag(Object tag);

    ElementLayout getResult();
}
