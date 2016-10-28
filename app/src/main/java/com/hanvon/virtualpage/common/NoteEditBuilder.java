package com.hanvon.virtualpage.common;

import android.content.Context;
import android.widget.AbsoluteLayout;

import com.hanvon.virtualpage.pageeditor.tools.text.NoteEditText;

/**
 * -------------------------------
 * Description:
 * 继承IElementBuilder，为创建文本提供方法，包括设置布局参数，添加边框，构建内容，设置tag信息等
 * -------------------------------
 */
public class NoteEditBuilder implements IElementBuilder {
    protected NoteEditText mNoteEdit;

    public NoteEditBuilder(Context c, int boundSize) {
        mNoteEdit = new NoteEditText(c, boundSize);
    }

    @Override
    public void setElementLayoutParams(AbsoluteLayout.LayoutParams params) {
        mNoteEdit.setLayoutParams(params);
    }

    @Override
    public void buildBounds() {
        mNoteEdit.addBounds();
    }

    @Override
    public void buildContentView() {
        mNoteEdit.addContentView();
    }

    @Override
    public void setElementTag(Object tag) {
        mNoteEdit.setTag(tag);
    }

    @Override
    public ElementLayout getResult() {
        return mNoteEdit;
    }

    @Override
    public void setElementHorizontalScrollBarEnabled(boolean flag) {
        mNoteEdit.setHorizontalScrollBarEnabled(flag);
    }
}

