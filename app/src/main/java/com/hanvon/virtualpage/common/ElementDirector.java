package com.hanvon.virtualpage.common;

import android.widget.AbsoluteLayout;

/**
 * -------------------------------
 * Description:
 *构造文本及图片的类，构造参数包括布局，及tag信息（各个对象不同）
 * -------------------------------
 */
public class ElementDirector {
    public void Construct(IElementBuilder builder, AbsoluteLayout.LayoutParams params, Object tag) {
        builder.setElementLayoutParams(params);
        builder.setElementTag(tag);
        builder.buildContentView();
        builder.setElementHorizontalScrollBarEnabled(false);
    }
}
