package com.hanvon.virtualpage.common;

import android.content.Context;
import android.widget.AbsoluteLayout;

import com.hanvon.virtualpage.pageeditor.tools.picture.NotePicture;

/**
 * -------------------------------
 * Description:
 * 继承IElementBuilder，为创建图片提供方法，包括设置布局参数，添加边框，构建内容，设置tag信息等
 * -------------------------------
 */
public class NotePictureBuilder implements IElementBuilder {

    protected NotePicture mNotePicture;

    public NotePictureBuilder(Context c, int width, int height, int boundSize) {
        mNotePicture = new NotePicture(c, width, height, boundSize);
    }

    @Override
    public void setElementLayoutParams(AbsoluteLayout.LayoutParams params) {
        mNotePicture.setLayoutParams(params);
    }

    @Override
    public void buildBounds() {
        mNotePicture.addBounds();
    }

    @Override
    public void buildContentView() {
        mNotePicture.addContentView();
    }

    @Override
    public void setElementTag(Object tag) {
        mNotePicture.setTag(tag);
    }

    @Override
    public ElementLayout getResult() {
        return mNotePicture;
    }

    @Override
    public void setElementHorizontalScrollBarEnabled(boolean flag) {
        mNotePicture.setHorizontalScrollBarEnabled(flag);
    }
}


