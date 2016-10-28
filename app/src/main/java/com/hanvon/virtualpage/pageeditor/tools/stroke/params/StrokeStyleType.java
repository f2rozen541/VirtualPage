package com.hanvon.virtualpage.pageeditor.tools.stroke.params;

/**
 * -------------------------------
 * Description:
 * StrokeAntiAlias: the style type of stroke
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
public enum StrokeStyleType {
    PENCIL(1),
    PEN(2),
    BRUSH(3);

    private final int mValue;

    private StrokeStyleType(int value) {
        mValue = value;
    }

    public int getmValue() {
        return this.mValue;
    }
}
