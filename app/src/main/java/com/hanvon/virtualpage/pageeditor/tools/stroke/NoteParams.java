package com.hanvon.virtualpage.pageeditor.tools.stroke;

import com.hanvon.virtualpage.common.ElementType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeAntiAlias;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeParams;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeStyleType;
import com.hanvon.virtualpage.pageeditor.tools.stroke.params.StrokeWidthType;

import java.util.Observable;

/**
 * -------------------------------
 * Description:
 * 参数处理类。
 * 提供方法：设置及获取当前对象类型；设置及获取笔记各属性值。
 * -------------------------------
 */
public class NoteParams extends Observable {
    private static NoteParams Instance = new NoteParams();

    private ElementType mElementType;
    private StrokeParams mStrokeParams;
    private int mBackgroundType;

    private NoteParams() {
        mElementType = ElementType.STROKE;
        mStrokeParams = new StrokeParams();
    }

    public static NoteParams getCurrentPenNoteParams() {
        return Instance;
    }

    public ElementType getCurrentElementType() {
        return mElementType;
    }

    public void setCurrentElementType(ElementType elementType) {
        mElementType = elementType;
        setChanged();
        notifyObservers(mElementType);
    }

    public void setCurrentStrokeParams(StrokeParams strokeParams) {
        mStrokeParams.setStrokeAntiAlias(strokeParams.getStrokeAntiAlias());
        mStrokeParams.setStrokeColor(strokeParams.getStrokeColor());
        mStrokeParams.setStrokeStyleType(strokeParams.getStrokeStyleType());
        mStrokeParams.setStrokeWidthType(strokeParams.getStrokeWidthType());
        setChanged();
        notifyObservers(mStrokeParams);
    }

    public StrokeParams getCurrentStrokeParams(){
        return mStrokeParams;
    }

    public void setCurrentStrokeAntiAlias(StrokeAntiAlias alias) {
        mStrokeParams.setStrokeAntiAlias(alias);
        setChanged();
        notifyObservers(mStrokeParams);
    }

    public void setCurrentStrokeColorType(int color) {
        mStrokeParams.setStrokeColor(color);
        setChanged();
        notifyObservers(mStrokeParams);
    }

    public void setCurrentStrokeStyleType(StrokeStyleType style) {
        mStrokeParams.setStrokeStyleType(style);
        setChanged();
        notifyObservers(mStrokeParams);
    }

    public void setCurrentStrokeWidthType(StrokeWidthType width) {
        mStrokeParams.setStrokeWidthType(width);
        setChanged();
        notifyObservers(mStrokeParams);
    }

    public void setCurrentBackgroundType(int background) {
        if (background != -1)
            mBackgroundType = background;
        setChanged();
        notifyObservers(mBackgroundType);
    }
}


