package com.hanvon.virtualpage.pageeditor.tools.stroke.params;

/**
 * -------------------------------
 * Description:
 *
 * -------------------------------
 * Author:  hanlili
 * Date:    2016/2/24
 * E_mail:  hanlili@hanwang.com.cn
 */
import android.graphics.Color;

public class StrokeParams {
    private StrokeAntiAlias mStrokeAntiAlias;
    private int mStrokeColorType;

    private StrokeStyleType mStrokeStyleType;
    private StrokeWidthType mStrokeWidthType;

    public StrokeParams() {
        setStrokeAntiAlias(StrokeAntiAlias.LEVEL3);
        setStrokeColor(Color.BLACK);
        setStrokeStyleType(StrokeStyleType.PEN);
        setStrokeWidthType(StrokeWidthType.MEDIUM);
    }

    public void setStrokeColor(int color) {
        this.mStrokeColorType = color;

    }

    public int getStrokeColor() {
        return mStrokeColorType;
    }

    public void setStrokeStyleType(StrokeStyleType mStrokeStyleType) {
        this.mStrokeStyleType = mStrokeStyleType;
    }

    public StrokeStyleType getStrokeStyleType() {
        return mStrokeStyleType;
    }

    public void setStrokeWidthType(StrokeWidthType mStrokeWidthType) {
        this.mStrokeWidthType = mStrokeWidthType;
    }

    public StrokeWidthType getStrokeWidthType() {
        return mStrokeWidthType;
    }

    public void setStrokeAntiAlias(StrokeAntiAlias mStrokeAntiAlias) {
        this.mStrokeAntiAlias = mStrokeAntiAlias;
    }

    public StrokeAntiAlias getStrokeAntiAlias() {
        return mStrokeAntiAlias;
    }
}

