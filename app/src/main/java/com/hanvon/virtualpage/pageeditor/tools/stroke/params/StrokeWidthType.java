package com.hanvon.virtualpage.pageeditor.tools.stroke.params;

/**
 * -------------------------------
 * Description:
 * StrokeAntiAlias: the width of stroke
 * -------------------------------
 * Author:  hll
 * Date:    2016/2/24
 */
public enum StrokeWidthType {
    THINNER(1),
    THIN(2),
    MEDIUM(3),
    WIDE(4),
    WIDER(5);

    private final int mValue;

    StrokeWidthType(int value) {
        mValue = value;
    }

    public int getmValue() {
        return this.mValue;
    }
}

